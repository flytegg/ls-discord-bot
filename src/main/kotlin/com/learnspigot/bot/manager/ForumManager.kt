package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.ReputationPoint
import com.learnspigot.bot.entity.UserProfile
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.util.SLF4J
import dev.morphia.Datastore
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.ThreadMember
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.stream.Collectors
import kotlin.time.Duration.Companion.seconds

class ForumManager(private val bot: JDA, private val datastore: Datastore, private val leaderboardManager: LeaderboardManager) {

    private val forumMessageCounts: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    private val logger by SLF4J
    private val helpChannel = bot.getForumChannelById(System.getenv("HELP_CHANNEL_ID"))!!
    private val dontReopen: MutableList<String> = mutableListOf()

    init {
        logger.info("Initiating ForumManager")
        bot.listener<MessageReceivedEvent> {
            val channel: ThreadChannel = it.channel as? ThreadChannel ?: return@listener
            if (channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) return@listener

            if (it.message.type == MessageType.CHANNEL_PINNED_ADD) {
                if (it.author == bot.selfUser) {
                    it.message.delete().queue()
                }
            }

            if (it.author.isBot) return@listener

            channel.history.retrievePast(2).queue { messages ->
                if (messages[0].type != MessageType.DEFAULT) return@queue
                if (messages.size < 2) messages[0].pin().queue()
            }

            forumMessageCounts.getOrPut(it.channel.id) { mutableMapOf() }.let { map ->
                map[it.author.id] =
                    forumMessageCounts.getOrPut(it.channel.id) { mutableMapOf() }.getOrDefault(it.author.id, 0) + 1
                forumMessageCounts[it.channel.id] = map
            }
        }
        bot.listener<ChannelUpdateArchivedEvent> {
            if(!it.newValue!!) return@listener
            val channel: ThreadChannel = it.channel as? ThreadChannel ?: return@listener
            if (channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) return@listener
            if(dontReopen.contains(channel.id)) return@listener
            channel.manager.setArchived(false).complete()
            closeThread(channel)
        }
        logger.info("Loading missing history from database")
        helpChannel.threadChannels
            .filter { !it.isArchived }
            .forEach { thread ->
                thread.retrieveThreadMembers().complete().forEach {
                    val profile = datastore.findUserProfile(it.id)
                    profile.messageHistory.forEach { message ->
                        if(message.channelId == thread.id)
                            forumMessageCounts.getOrPut(thread.id) { mutableMapOf() }.let { map ->
                                map[it.id] = forumMessageCounts.getOrPut(thread.id) { mutableMapOf() }.getOrDefault(it.id, 0) + 1
                                forumMessageCounts[thread.id] = map
                            }
                    }
                }
            }
    }

    private fun getMessageCount(channel: ThreadChannel, member: ThreadMember): Int {
        return forumMessageCounts.getOrDefault(channel.id, mutableMapOf()).getOrDefault(member.id, 0)
    }

    fun closeThread(channel: ThreadChannel, forceClose: Boolean = false) {
        check(channel.parentChannel.id == System.getenv("HELP_CHANNEL_ID"))

        if(forceClose) {
            dontReopen.add(channel.id)
            channel.manager.setArchived(true).setLocked(true).queue()
            bot.removeEventListener(this)
            return
        }

        val contributors = channel.retrieveThreadMembers().complete()
            .filter { it.id != channel.ownerId }
            .filter { !it.user.isBot }
            .sortedWith { m1, m2 ->
                getMessageCount(channel, m1) compareTo getMessageCount(channel, m2)
            }
            .take(25) // No more than 25 users can be displayed in dropdown
        val eventSession = UUID.randomUUID()
        val removeOnClose: MutableList<Message> = mutableListOf()
        if(contributors.isNotEmpty()) {
            channel.sendMessageEmbeds(Embed {
                title = "Select contributors"
                description = "Use the dropdown to select the people who helped you"
                color = LearnSpigotBot.EMBED_COLOR
            }).addActionRow(StringSelectMenu(
                "contributors-${channel.id}-${channel.ownerId}-$eventSession",
                valueRange = 0..25,
                options = contributors.map { SelectOption.of(it.member.effectiveName, it.id) }
            )).complete().let {removeOnClose.add(it)}
        }
       channel.sendMessageEmbeds(Embed {
            description = if(contributors.isNotEmpty()) "Once you've selected contributors, click below to close your post." else "Please confirm to close"
            color = LearnSpigotBot.EMBED_COLOR
        }).addActionRow(danger("close-${channel.id}-$eventSession", "Close")).complete().let {removeOnClose.add(it)}

        var selectedContributors: List<String> = emptyList()
        bot.listener<StringSelectInteractionEvent> { event ->
            if(event.componentId != "contributors-${channel.id}-${channel.ownerId}-$eventSession") return@listener
            if(channel.isArchived) return@listener
            if(event.member!!.id == channel.ownerId || event.member!!.hasPermission(Permission.MANAGE_SERVER)) {
                selectedContributors = event.values
                event.deferEdit().queue()
            }
        }

        bot.listener<ButtonInteractionEvent> { event ->
            if(event.button.id!! != "close-${channel.id}-$eventSession") return@listener
            if(event.member!!.id == channel.ownerId || event.member!!.hasPermission(Permission.MANAGE_THREADS)) {
                dontReopen.add(channel.id)
                event.editButton(event.button.asDisabled()).complete()
                selectedContributors.forEach {
                    val profile: UserProfile = datastore.findUserProfile(it)
                    profile.addRep(channel, channel.owner!!, leaderboardManager, event.guild!!)
                    datastore.save(profile)
                }
                removeOnClose.forEach { message -> message.delete().complete()}
                channel.sendMessageEmbeds(Embed {
                    description = "${event.member!!.asMention} has closed the thread."
                    description += "\n\nListing " +
                            if (selectedContributors.isEmpty()) "no contributors"
                            else {
                                val contributorString = selectedContributors.stream().map {contributor -> bot.getUserById(contributor)?.asMention}
                                    .collect(Collectors.joining(", "))
                                val buffer = StringBuffer(contributorString)
                                val lastIndex = contributorString.lastIndexOf(",")
                                if(lastIndex != -1) {
                                    buffer.replace(lastIndex, lastIndex + 1, " and")
                                }
                                val string = " as " + if(selectedContributors.size == 1) "a contributor." else "contributors."
                                buffer.toString() + string
                            }
                    color = LearnSpigotBot.EMBED_COLOR
                }).complete()
                channel.manager.setArchived(true).setLocked(true).queue()
                bot.removeEventListener(this)
            } else {
                event.replyEmbed({
                    description = "You cannot close the thread as you are not the author!"
                    color = LearnSpigotBot.EMBED_COLOR
                }, ephemeral = true).queue()
            }
        }
    }

    fun migrateReputation() {
        logger.info("Migration rep")
        val guild = bot.getGuildById(System.getenv("GUILD"))!!
        val supportChannel: TextChannel = guild.getTextChannelById(System.getenv("SUPPORT_CHANNEL_ID"))!!

        var total = 0
        val startingTime = System.currentTimeMillis()
        supportChannel.iterableHistory.reverse().skipTo(1037138537457389628).cache(false).forEachAsync {
            if(it.timeCreated.isAfter(OffsetDateTime.of(2022, 11, 1, 10, 0, 0 ,0, ZoneOffset.UTC))) {
                if(it.timeCreated.isBefore(OffsetDateTime.of(2022, 12, 1, 10, 0, 0, 0, ZoneOffset.UTC))) {
                    if(it.author.id == bot.selfUser.id) {
                        if(it.embeds.size > 0) {
                            it.embeds.forEach { embed ->
                                if(embed.title == "Reputation Added!") {
                                    val description = embed.description!!.split(Regex.fromLiteral(" "))
                                    val targetId = description[0].replace("<", "").replace(">", "").replace("@", "")
                                    val postId = description[7].replace("<", "").replace(">", "").replace("#", "")

                                    val repPoint = ReputationPoint(
                                        it.timeCreated.toEpochSecond().seconds.inWholeMilliseconds,
                                        null,
                                        if(postId == "0") null else postId
                                    )
                                    val profile = datastore.findUserProfile(targetId)
                                    profile.reputation.add(repPoint)
                                    datastore.save(profile)
                                    total++
                                    logger.info("Added reputation ($total)")
                                }
                            }
                        }
                    }
                }
            }
            return@forEachAsync true
        }.thenRun {
            logger.info("Finished in ${System.currentTimeMillis() - startingTime}ms")
            logger.info("Added a total of $total")
        }
    }
}
