package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class PollManager(bot: JDA) {

    private val pollMessages: MutableMap<String, ScheduledFuture<*>> = mutableMapOf()

    init {
        bot.listener<MessageReactionAddEvent> {
            if(pollMessages.keys.contains(it.messageId)) {
                val message = it.retrieveMessage().complete()
                message.reactions.forEach { reaction ->
                    if(reaction.emoji.name != it.emoji.name) {
                        val reactors = reaction.retrieveUsers().complete()
                        reactors.forEach { reactor ->
                            if(reactor.id == it.userId) reaction.removeReaction(reactor).queue()
                        }
                    }
                }
            }
        }
    }

    fun postPoll(channel: TextChannel, question: String, time: Long, options: List<Emoji>) {
        val message = channel.sendMessageEmbeds(Embed {
            title = "Poll"
            description = question
            color = LearnSpigotBot.EMBED_COLOR
        }).complete()
        println("$time ms (${time.milliseconds.inWholeHours} hours)")
        options.forEach {
            message.addReaction(it).complete()
        }
        pollMessages[message.id] = message.clearReactions().queueAfter(time, TimeUnit.MILLISECONDS) {
            channel.sendMessage("Poll ended").queue()
        }
    }
}