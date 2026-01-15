package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessagePollData
import java.awt.Color
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class ReputationVotesListener : ListenerAdapter() {

    val ThreadChannel.isKnowledgebase get() = parentChannel.isChannel(Server.CHANNEL_KNOWLEDGEBASE)
    val ThreadChannel.isProjects get() = parentChannel.isChannel(Server.CHANNEL_PROJECTS)

    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel()
        val isKnowledgebase = thread.isKnowledgebase
        if (!isKnowledgebase && !thread.isProjects) return

        val poll = MessagePollData.builder("How much reputation should be given?")
            .setDuration(Duration.ofHours(24))

        for (amount in 1 until if (isKnowledgebase) 6 else 10)
            poll.addAnswer("$amount reputation", Emoji.fromUnicode("${amount}\u20E3"))

        Server.CHANNEL_VOTES.sendMessageEmbeds(
            embed()
                .setTitle("New ${if (isKnowledgebase) "knowledgebase" else "project"} post!")
                .setDescription("""
                    Vote how much reputation should get?
                    ${event.channel.asMention}
                """.trimIndent())
                .build()
        ).setPoll(poll.build()).queue()
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.type != MessageType.POLL_RESULT) return
        if (!event.channel.isChannel(Server.CHANNEL_VOTES)) return
        val pollResult = event.message.messageReference ?: return
        event.channel.retrieveMessageById(pollResult.messageId).queue { poll ->
            val channel = poll.getFormChannel() ?: return@queue
            val owner = channel.owner ?: return@queue
            val answers = poll.poll?.answers ?: return@queue

            val pendingRequests = AtomicInteger(answers.size)
            var highestVotes = 0
            var repAmount = 0

            for (answer in answers) {
                poll.retrievePollVoters(answer.id).queue({ users ->
                    val amount = users.filter { it.id != owner.id }.size

                    synchronized(this) {
                        if (amount > highestVotes) {
                            highestVotes = amount
                            repAmount = answers.indexOf(answer) + 1
                        }

                        if (pendingRequests.decrementAndGet() != 0) return@synchronized
                        poll.delete().queue()
                        event.message.delete().queue()

                        if (repAmount == 0) return@synchronized
                        Registry.PROFILES.findByUser(owner.user).addReputation(
                            owner.user,
                            channel.ownerId,
                            channel.id,
                            repAmount
                        )
                    }
                })
            }
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.channel.isChannel(Server.CHANNEL_VOTES)) return
        val member = event.member ?: return
        if (!member.roles.contains(Server.ROLE_MANAGEMENT)) return
        if (event.emoji != Server.EMOJI_DOWNVOTE) return
        event.retrieveMessage().queue { message ->
            val form = message.getFormChannel()
            form?.delete()?.queue()
            message.delete().queue()
            form?.owner?.user?.openPrivateChannel()?.queue { privateChannel ->
                privateChannel.sendMessageEmbeds(
                    embed().setTitle("Your ${if (form.isKnowledgebase) "knowledgebase" else "project"} post has been deleted!")
                        .setDescription("Your post has been review by management and deemed not up to standard.")
                        .setColor(Color.RED)
                        .build()
                ).queue()
            }
        }
    }

    private fun Message.getFormChannel(): ThreadChannel? = embeds[0].description?.lines()?.get(1)?.substring(2, 21)?.let{ id -> Server.GUILD.getChannelById(ThreadChannel::class.java, id) }
}