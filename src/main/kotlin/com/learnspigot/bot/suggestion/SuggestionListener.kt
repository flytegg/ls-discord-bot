package com.learnspigot.bot.suggestion

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SuggestionListener : ListenerAdapter() {

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.author.isBot) return
        if (!e.isFromGuild) return
        if (e.guild.id != Server.guildId) return
        if (e.channel.id != Environment.get("SUGGESTIONS_CHANNEL_ID")) return

        e.message.apply {
            addReaction(Server.upvoteEmoji).queue()
            addReaction(Server.downvoteEmoji).queue()
            createThreadChannel("Suggestion from ${e.author.effectiveName}").queue()
        }
    }

}