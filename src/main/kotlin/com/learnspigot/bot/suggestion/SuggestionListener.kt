package com.learnspigot.bot.suggestion

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SuggestionListener : ListenerAdapter() {

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.author.isBot) return
        if (!e.isFromGuild) return
        if (e.guild.id != Server.GUILD_ID) return
        if (e.channel.id != Environment.get("SUGGESTIONS_CHANNEL_ID")) return

        e.message.apply {
            addReaction(Server.EMOJI_UPVOTE).queue()
            addReaction(Server.EMOJI_DOWNVOTE).queue()
            createThreadChannel("Suggestion from ${e.author.effectiveName}").queue()
        }
    }

}