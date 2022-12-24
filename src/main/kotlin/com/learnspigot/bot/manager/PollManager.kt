package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import kotlin.time.Duration.Companion.milliseconds

class PollManager(bot: JDA) {

    private val pollMessages: MutableMap<String, Job> = mutableMapOf()

    init {
        bot.listener<MessageReactionAddEvent> {
            if(it.userId == bot.selfUser.id) return@listener
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

    @OptIn(DelicateCoroutinesApi::class)
    fun postPoll(channel: TextChannel, question: String, time: Long, options: List<Emoji>) {
        val message = channel.sendMessageEmbeds(Embed {
            title = "Poll"
            description = "$question \n\nEnds <t:${(System.currentTimeMillis() + time).milliseconds.inWholeSeconds}:R>"
            color = EMBED_COLOR
        }).complete()
        options.forEach {
            message.addReaction(it).complete()
        }
        GlobalScope.launch {
            pollMessages[message.id] = async {
                delay(time)
                message.editMessageEmbeds(Embed {
                    title = "Poll (Finished)"
                    description = question
                    description += "\n\n**Poll Complete**"
                    color = EMBED_COLOR

                    val pollMessage = channel.retrieveMessageById(message.id).complete()
                    pollMessage.reactions
                        .sortedWith { r1, r2 ->
                            r1.retrieveUsers().complete().size compareTo r2.retrieveUsers().complete().size
                        }
                        .forEach { reaction ->
                            println("reaction ")
                            field("Votes for ${reaction.emoji.formatted}", (reaction.retrieveUsers().complete().size - 1).toString())
                        }
                }).queue {
                    message.clearReactions().queue()
                }
            }
        }
    }
}