package com.learnspigot.bot.suggestion

import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class SuggestionListener : ListenerAdapter() {

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.author.isBot) return
        if (!e.isFromGuild) return
        if (e.guild.id != System.getenv("GUILD_ID")) return
        if (e.channel.id != System.getenv("SUGGESTIONS_CHANNEL_ID")) return

        val content = e.message.contentDisplay
        e.message.delete().queue()

        if (content.length < 6) {
            e.channel.sendMessageEmbeds(embed().setDescription("Please explain your suggestion properly.").build())
                .queue { message: Message ->
                    message.delete().queueAfter(3, TimeUnit.SECONDS)
                }
            return
        }

        e.channel.sendMessageEmbeds(
            embed()
                .setTitle("Suggestion")
                .setDescription(content)
                .addField("Submitted by", e.author.asMention, false)
                .build()
        ).queue { message: Message ->
            message.addReaction(Emoji.fromCustom("upvote", System.getenv("UPVOTE_EMOJI_ID").toLong(), false)).queue()
            message.addReaction(Emoji.fromCustom("downvote", System.getenv("DOWNVOTE_EMOJI_ID").toLong(), false))
                .queue()
            message.createThreadChannel("Discussion").queue()
        }
    }

}