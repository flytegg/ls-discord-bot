package com.learnspigot.bot

import com.learnspigot.bot.command.*
import com.learnspigot.bot.dbmigrator.migrations.UserProfileMigrator
import com.learnspigot.bot.entity.DataFile
import com.learnspigot.bot.entity.Giveaway
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.http.UdemyService
import com.learnspigot.bot.listener.UserListeners
import com.learnspigot.bot.manager.*
import com.learnspigot.bot.util.LectureSearcher
import com.mongodb.client.MongoClients
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.cache
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.*
import dev.minn.jda.ktx.util.SLF4J
import dev.morphia.Datastore
import dev.morphia.Morphia
import dev.morphia.mapping.MapperOptions
import dev.morphia.query.experimental.filters.Filter
import dev.morphia.query.experimental.filters.Filters
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class LearnSpigotBot {

    private val activities = arrayOf(
        Activity.watching("Merlin's Mum"), Activity.playing("IntelliJ Idea"), Activity.watching("You..."),
        Activity.listening("Your questions")
    )

    private val datastore: Datastore = Morphia.createDatastore(
        MongoClients.create(System.getenv("MONGO_URI")!!), System.getenv("MONGO_DB")!!,
        MapperOptions.builder().storeEmpties(true).build()
    )
    val bot = light(System.getenv("DISCORD_TOKEN")!!) {
            enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
            cache += CacheFlag.FORUM_TAGS
            setMemberCachePolicy(MemberCachePolicy.ALL)
            setChunkingFilter(ChunkingFilter.ALL)
            setActivity(activities[0])
        }.also { logger.info("Logged into ${it.selfUser.name}#${it.selfUser.discriminator}") }
    val guild: Guild
    private val forumManager: ForumManager
    private val lectureSearcher: LectureSearcher
    private val verificationManager: VerificationManager
    private val leaderboardManager: LeaderboardManager
    private val giveawayManager: GiveawayManager
    private val pollManager: PollManager
    private val data: DataFile = FileManager.loadConfig("data.json")

    private lateinit var activityJob: Job

    init {
        logger.info("Checking for migrations")
        if(!data.migrated) {
            UserProfileMigrator().scanDatabase(datastore.database)
            data.migrated = true
            FileManager.saveConfig("data.json", data)
            logger.info("Migrated!")
        }

        logger.info("Mapping entities")
        datastore.mapper.mapPackage("com.learnspigot.bot.entity")
        datastore.ensureIndexes()

        bot.awaitReady()
        guild = bot.getGuildById(System.getenv("GUILD"))!!
        leaderboardManager = LeaderboardManager(bot, datastore)
        lectureSearcher = LectureSearcher(UdemyService())
        forumManager = ForumManager(bot, datastore, leaderboardManager)
        verificationManager = VerificationManager(datastore)
        giveawayManager = GiveawayManager(bot, datastore)
        pollManager = PollManager(bot)
        registerCommands()
        registerListeners()

        GlobalScope.launch {
            activityJob = async {
                while (true) {
                    for (activity in activities) {
                        bot.presence.activity = activity
                        delay(60.seconds.inWholeMilliseconds)
                    }
                }
            }
        }
        if(!data.migratedRep) {
            forumManager.migrateReputation()
            data.migratedRep = true
            FileManager.saveConfig("data.json", data)
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                logger.info("Shutdown signal received. Gracefully shutting down.")

                activityJob.cancel()
                withTimeoutOrNull(5.seconds.inWholeMilliseconds) {
                    bot.shutdown()
                } ?: run { // 5 second timeout
                    logger.error("Timed out waiting for ${5.seconds.inWholeMilliseconds} ms")
                    logger.error("Forcefully disconnecting from discord")
                    bot.shutdownNow()
                }

                leaderboardManager.updateJob.cancel()
                leaderboardManager.saveJob.cancel()
                leaderboardManager.saveMessages()
            }
        })
    }

    private fun registerCommands() {
        logger.info("Registering commands...")
//        bot.retrieveCommands().queue {
//            it.forEach { it.delete().queue() }
//        }
        bot.listener<GenericCommandInteractionEvent> {
            logger.info("${it.user.name}#${it.user.discriminator} executed ${it.commandString}")
        }
        VerifyCommand(guild, bot, verificationManager).apply {
            adminVerifyCommand()
            verifyUserContext()
            forceVerifyContext()
            addVerifyEmbedCommand()
        }
        UnverifyCommand(guild, bot, verificationManager).apply {
            unverifyCommand()
            unverifyContext()
        }
        LectureCommand(guild, bot, lectureSearcher).apply {
            searchLecturesCommand()
            searchQuizzesCommand()
        }
        ReputationCommand(guild, bot, datastore, leaderboardManager).apply {
            repCommand()
            repLeaderboardCommand()
            repAdminCommand()
            addLeaderboardCommand()
            addLookupCommand()
            addRepContext()
            removeRepContext()
        }
        ToggleCommand(guild, bot, datastore).apply {
            toggleCommand()
        }
        ThreadCloseCommand(guild, bot, forumManager)
        ProfileCommand(guild, bot, datastore)
        SuggestionsCommand(guild, bot)
        TeslaStockCommand(guild, bot)
        VersionCommand(guild, bot)
        StatisticCommand(guild, bot, datastore)
        GiveawayCommand(guild, bot, giveawayManager)
        PollCommand(bot, pollManager)
    }

    private fun registerListeners() {
        logger.info("Registering listeners...")
        UserListeners(guild, bot, datastore)
    }


    companion object {
        const val LEARN_SPIGOT_COURSE_ID: Long = 1093774
        const val EMBED_COLOR = 0x2F3136
        val logger: Logger by SLF4J

        inline fun <reified T : Any> Datastore.findOne(filter: Filter): T? {
            return find(T::class.java)
                .filter(filter)
                .toList()
                .getOrNull(0)
        }

        fun Datastore.findGiveaway(id: String): Giveaway {
            return find(Giveaway::class.java)
                .filter(Filters.eq("_id", id))
                .toList()
                .getOrElse(0) {
                    Giveaway(id).also { save(it) }
                }
        }

        fun Datastore.findUserProfile(id: String): UserProfile {
            return find(UserProfile::class.java)
                .filter(Filters.eq("_id", id))
                .toList()
                .getOrElse(0) {
                    UserProfile(id).also { save(it) }
                }
        }

        val User.nameAndTag: String
            get() = "$name#$discriminator"

        fun IReplyCallback.editEmbed(
            builder: InlineEmbed.() -> Unit,
            content: String = SendDefaults.content,
            components: Collection<LayoutComponent> = SendDefaults.components,
            files: Collection<FileUpload> = emptyList(),
            mentions: Mentions = Mentions.default(),
        ): WebhookMessageEditAction<Message> {
            val embed = InlineEmbed(Embed(color = EMBED_COLOR))
            builder.invoke(embed)
            return hook.editOriginal(MessageEdit(content, listOf(embed.build()), files, components, mentions))
        }

        fun IReplyCallback.replyEmbed(
            builder: InlineEmbed.() -> Unit,
            content: String = SendDefaults.content,
            components: Collection<LayoutComponent> = SendDefaults.components,
            files: Collection<FileUpload> = emptyList(),
            tts: Boolean = false,
            mentions: Mentions = Mentions.default(),
            ephemeral: Boolean = SendDefaults.ephemeral,
        ): ReplyCallbackAction {
            val embed = InlineEmbed(Embed(color = EMBED_COLOR))
            builder.invoke(embed)
            return reply_(content, listOf(embed.build()), components, files, tts, mentions, ephemeral)
        }
    }
}