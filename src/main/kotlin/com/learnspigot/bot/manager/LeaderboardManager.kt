package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.LearnSpigotBot.Companion.nameAndTag
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.DataFile
import com.learnspigot.bot.entity.UserProfile
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import dev.morphia.Datastore
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.components.ActionRow
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class LeaderboardManager(bot: JDA, private val datastore: Datastore) {

    private val medals: Array<String> = arrayOf(":first_place:", ":second_place:", ":third_place:")

    private val data: DataFile = FileManager.loadConfig("data.json")
    private val guild = bot.getGuildById(System.getenv("GUILD"))!!
    private val allTimeLeaderboards: MutableList<Message> = loadMessages(data.allTimeLeaderboards)
    private val monthlyLeaderboards: MutableList<Message> = loadMessages(data.monthlyLeaderboards)
    private val lookupMessages: MutableList<Message> = loadMessages(data.lookupMessages)
    private val logger by SLF4J

    lateinit var updateJob: Job
    lateinit var saveJob: Job
    lateinit var updateLookupJob: Job

    init {
        bot.listener<StringSelectInteractionEvent> {
            if(it.componentId != "lookup") return@listener
            val profile: UserProfile = datastore.findUserProfile(it.values[0])
            val target = guild.getMemberById(profile.id)!!
            it.replyEmbed({
                title = "Reputation"
                description = "${target.asMention} has ${profile.reputation.size} reputation points"
                profile.reputation.sortedWith { o1, o2 ->
                    o1.timestamp() compareTo o2.timestamp()
                }.take(3).forEach { rep ->
                    description +="\n\u2022 "
                    if(rep.postId != null) {
                        description += "In <#${rep.postId}>, "
                    }

                    if(rep.fromMemberId != null) {
                        description += "from <@${rep.fromMemberId}> "
                    }

                    description += "at <t:${rep.epochTimestamp.milliseconds.inWholeSeconds}:f>"
                }
            }, ephemeral = true).queue()
        }
        GlobalScope.launch {
            activateJobs()
        }
    }

    private suspend fun activateJobs() = withContext(Dispatchers.Default) {
        updateJob = async {
            while (true) {
                updateLeaderboardMessages()
                //delay(10.minutes.inWholeMilliseconds)
                delay(60.seconds.inWholeMilliseconds)
            }
        }
        saveJob = async {
            while (true) {
                saveMessages()
                //delay(30.minutes.inWholeMilliseconds)
                delay(60.seconds.inWholeMilliseconds)
            }
        }
        updateLookupJob = async {
            while (true) {
                // Update options
                lookupMessages.forEach {
                    it.editMessageComponents(buildLookupComponent()).queue()
                }
                delay(60.seconds.inWholeMilliseconds)
            }
        }
    }

    fun updateLeaderboardMessages() {
        logger.info("Updating leaderboards (${allTimeLeaderboards.size})")
        val toRemove: MutableList<Message> = mutableListOf()

        allTimeLeaderboards.forEach {
            runCatching {
                it.editMessageEmbeds(buildMessage(false)).queue()
            }.onFailure { _ ->
                logger.error("Unable to find message with id ${it.id}. This message id will be lost at next save.")
                toRemove.add(it)
            }
        }
        monthlyLeaderboards.forEach {
            runCatching {
                it.editMessageEmbeds(buildMessage(true)).queue()
            }.onFailure { _ ->
                logger.error("Unable to find message with id ${it.id}. This message id will be lost at next save.")
                toRemove.add(it)
            }
        }

        toRemove.forEach { _ ->
            allTimeLeaderboards.removeAll(toRemove)
            monthlyLeaderboards.removeAll(toRemove)
        }
    }

    private fun loadMessages(idList: List<String>): MutableList<Message> {
        val res: MutableList<Message> = mutableListOf()
        idList.forEach { message ->
            var found = false
            guild.channels.filter { it is MessageChannel }.map { it as MessageChannel }
                .forEach {
                    try {
                        res.add(it.retrieveMessageById(message).complete())
                        found = true
                    }catch (ex: ErrorResponseException) {
                        if(ex.errorCode != 10_008) { // Unknown message error code
                            throw ex
                        }
                    }
                }
            if(!found) {
                logger.error("Unable to find message with id $message. This message id will be lost at next save.")
            }
        }
        return res
    }

    fun createMessage(channel: MessageChannel, monthly: Boolean = false) {
        channel.sendMessageEmbeds(buildMessage(monthly)).queue {
            if(monthly) {
                monthlyLeaderboards.add(it)
            }else {
                allTimeLeaderboards.add(it)
            }
            saveMessages()
        }
    }

    fun saveMessages() {
        logger.info("Saving messages")
        data.allTimeLeaderboards = allTimeLeaderboards.map { it.id }
        data.monthlyLeaderboards = monthlyLeaderboards.map { it.id }
        data.lookupMessages = lookupMessages.map { it.id }
        FileManager.saveConfig("data.json", data)
    }

    private fun buildMessage(monthly: Boolean): MessageEmbed {
        val topUsers = datastore.find(UserProfile::class.java)
            .filter { it.reputation.size >= 1 }
            .map {
                if(!monthly) return@map it
                return@map UserProfile(
                    it.id,
                    it.udemyUrl,
                    it.reputation.filter { rep ->
                        YearMonth.now()
                            .atDay(1)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                            .isBefore(rep.timestamp())
                    }.toMutableList(),
                    it.messageHistory
                )
            }
            .sortedWith { o1, o2 ->
                o1.reputation.size compareTo o2.reputation.size
            }
            .reversed()
            .take(10)
        return Embed {
            title = if(monthly) "Monthly Leaderboard" else "All-Time Leaderboard"
            color = EMBED_COLOR
            description = if(monthly) "These stats are reset every month. \n" else ""
            topUsers.forEachIndexed { i, profile ->
                val username = guild.getMemberById(profile.id)?.asMention ?: "*User not found* (`${profile.id}`)"
                val medal: String = if((i + 1) <= medals.size) {
                    medals[i]
                }else {
                    ""
                }
                description += "\n${i + 1}. $username - ${profile.reputation.size} $medal"
            }
            description += "\n\nLast updated: <t:${Instant.now().epochSecond}:t>"
        }
    }

    fun sendLookupMessage(channel: MessageChannel) {
        channel.sendMessageEmbeds(Embed {
            title = "Reputation Searcher"
            description = "Choose a support member below to find their rep"
            color = EMBED_COLOR
        }).addActionRow(buildLookupComponent().components)
            .queue {
                lookupMessages.add(it)
            }
    }

    private fun buildLookupComponent(): ActionRow {
        val topUsers = datastore.find(UserProfile::class.java)
            .filter { it.reputation.size >= 1 }
            .filter {
                val member = guild.getMemberById(it.id) ?: return@filter false
                val supportRole = guild.getRoleById(System.getenv("SUPPORT_ROLE_ID"))!!
                return@filter member.roles.map { it.id }.contains(supportRole.id)
            }
            .map {
                return@map UserProfile(
                    it.id,
                    it.udemyUrl,
                    it.reputation.filter { rep ->
                        YearMonth.now()
                            .atDay(1)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC)
                            .isBefore(rep.timestamp())
                    }.toMutableList(),
                    it.messageHistory
                )
            }
            .sortedWith { o1, o2 ->
                o1.reputation.size compareTo o2.reputation.size
            }
            .reversed()
            .take(25)

        return ActionRow.of(StringSelectMenu("lookup") {
            topUsers.forEach {
                val member = guild.getMemberById(it.id)!!
                addOption(member.effectiveName, it.id, member.user.nameAndTag)
            }
        })
    }
}