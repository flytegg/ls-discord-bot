package com.learnspigot.bot.counting

import com.github.mlgpenguin.mathevaluator.Evaluator
import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

class CountingListener: ListenerAdapter() {

    val currentCount: Int get() = Registry.COUNTING.currentCount

    var lastCount: Message? = null

    fun fuckedUp(user: User) {
        lastCount = null
        Registry.COUNTING.fuckedUp(user)
    }

    val insults = CountingInsults()

    private fun Channel.isCounting() = isChannel(Server.CHANNEL_COUNTING)
    private fun Message.millisSinceLastCount() = timeCreated.toInstant().toEpochMilli() - (lastCount?.timeCreated?.toInstant()?.toEpochMilli() ?: 0)
    private fun User.hasFailedBefore() = Registry.PROFILES.findByUser(this).countingFuckUps > 0

    private val thinking = Emoji.fromUnicode("🤔")
    private val oneHundred = Emoji.fromUnicode("💯")

    val newbieDoubleCountEmbed = EmbedBuilder()
        .setColor(Color.YELLOW)
        .setTitle("Warning")
        .setDescription("**You CANNOT count twice in a row.**\n\n The next number is *still* ${currentCount+1}.")
        .build()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isFromGuild || !event.channel.isCounting() || event.guild.id != Server.GUILD_ID) return
        if (event.message.attachments.isNotEmpty()) return

        val msg = event.message.contentRaw
        val userId = event.author.id
        if (Evaluator.isValidSyntax(msg)) {
            val evaluated = Evaluator.eval(msg).intValue()
            if (evaluated == currentCount + 1) {
                // User has counted twice in a row, which, for those of you who pay attention, is STRICTLY against the rules of counting.
                if (userId.equals(lastCount?.author?.id, true)) {
                    // This should, in theory, make the dumbfoundingly obvious rule, even more fucking obvious.
                    if (!event.author.hasFailedBefore()) {
                        event.message.addReaction(thinking).queue()
                        event.message.replyEmbeds(newbieDoubleCountEmbed).queue()
                        return
                    }

                    event.message.addReaction(Server.EMOJI_DOWNVOTE)
                    val insultMessage = insults.doubleCountInsults.random()
                    event.message.reply("$insultMessage ${event.author.asMention}, The count has been reset to 1.").queue()
                    fuckedUp(event.author)
                    return
                }
                val reactionEmoji = if (evaluated % 100 == 0) oneHundred else Server.EMOJI_UPVOTE


                lastCount = event.message
                event.message.addReaction(reactionEmoji).queue()
                Registry.COUNTING.incrementCount(event.author)

            } else {
                if (evaluated == currentCount && event.message.millisSinceLastCount() < 600) {
                    // ( 600ms delay ) - Arbitrary value based on superficial testing
                    event.message.addReaction(thinking).queue()
                    event.message.reply("I'll let this one slide").queue()
                    return
                }

                val next = currentCount + 1
                fuckedUp(event.author)
                event.message.addReaction(Server.EMOJI_DOWNVOTE).queue()

                val insultMessage = insults.fuckedUpInsults.random()

                event.message.reply("$insultMessage ${event.author.asMention}, The next number was $next, not $evaluated.").queue()
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!event.channel.isCounting()) return
        if (event.messageId == lastCount?.id) {
            Server.CHANNEL_COUNTING.sendMessage("${lastCount?.author?.asMention} deleted their count of $currentCount").queue()
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!event.channel.isCounting()) return
        if (event.messageId == lastCount?.id) {
            Server.CHANNEL_COUNTING.sendMessage("${event.author.asMention} edited their count of $currentCount").queue()
        }
    }

}
