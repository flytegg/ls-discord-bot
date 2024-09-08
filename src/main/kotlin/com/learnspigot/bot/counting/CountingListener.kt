package com.learnspigot.bot.counting

import com.github.mlgpenguin.mathevaluator.Evaluator
import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.profile.ProfileRegistry
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.LocalDateTime
import kotlin.math.ceil

class CountingListener : ListenerAdapter() {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Inject
    private lateinit var countingRegistry: CountingRegistry

    val currentCount: Int get() = countingRegistry.currentCount

    var lastCount: Message? = null

    val hoursMute: Int
        get() = (ceil((currentCount - 50) / 75.0) * 12).toInt()

    fun fuckedUp(user: User) {
        countingRegistry.fuckedUp(user)
        lastCount = null
    }

    private fun Channel.isCounting() = id == Environment.get("COUNTING_CHANNEL_ID")
    private fun Message.millisSinceLastCount() =
        timeCreated.toInstant().toEpochMilli() - (lastCount?.timeCreated?.toInstant()?.toEpochMilli() ?: 0)

    private val thinking = Emoji.fromUnicode("🤔")
    private val oneHundred = Emoji.fromUnicode("💯")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.member == null || !event.isFromGuild || !event.channel.isCounting() || event.guild.id != Server.guildId) return
        if (event.message.attachments.isNotEmpty()) return

        val msg = event.message.contentRaw
        val userId = event.author.id
        if (Evaluator.isValidSyntax(msg)) {
            val userMuted = profileRegistry.findByUser(event.member!!.user).muteExpiry

            if (userMuted != 0) {
                val timeNow = LocalDateTime.now()
                val muteExpiry = LocalDateTime.now().plusSeconds(userMuted.toLong())

                if (!timeNow.isAfter(muteExpiry)) {
                    event.message.delete().queue()
                    return
                }

                profileRegistry.findByUser(event.member!!.user).muteExpiry = 0
            }

            val evaluated = Evaluator.eval(msg).intValue()
            if (evaluated == currentCount + 1) {
                if (userId.equals(lastCount?.author?.id, true)) return run {
                    event.message.addReaction(Server.downvoteEmoji)

                    val insultMessage = CountingInsults.doubleCountInsults.random()
                    var message = "$insultMessage ${event.author.asMention}, The count has been reset to 1."

                    if (currentCount >= 50) message += " You are banned from counting for $hoursMute hours!"

                    event.message.reply(message).queue()

                    fuckedUp(event.author)
                }
                val reactionEmoji = if (evaluated % 100 == 0) oneHundred else Server.upvoteEmoji


                lastCount = event.message
                event.message.addReaction(reactionEmoji).queue()
                countingRegistry.incrementCount(event.author)

            } else {
                if (evaluated == currentCount && event.message.millisSinceLastCount() < 600) {
                    // ( 600ms delay ) - Arbitrary value based on superficial testing
                    event.message.addReaction(thinking).queue()
                    event.message.reply("I'll let this one slide").queue()
                    return
                }

                val next = currentCount + 1

                event.message.addReaction(Server.downvoteEmoji).queue()

                val insultMessage = CountingInsults.doubleCountInsults.random()
                var message = "$insultMessage ${event.author.asMention}, The next number was $next, not $evaluated."

                if (currentCount >= 50) message += " You are banned from counting for $hoursMute hours!"

                event.message.reply(message).queue()

                fuckedUp(event.author)
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!event.channel.isCounting()) return
        if (event.messageId == lastCount?.id) {
            Server.countingChannel.sendMessage("${lastCount?.author?.asMention} deleted their count of $currentCount").queue()
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!event.channel.isCounting()) return
        if (event.messageId == lastCount?.id) {
            Server.countingChannel.sendMessage("${event.author.asMention} edited their count of $currentCount").queue()
        }
    }

}
