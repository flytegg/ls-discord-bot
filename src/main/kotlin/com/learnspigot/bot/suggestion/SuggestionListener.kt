package com.learnspigot.bot.suggestion

import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isChannel
import com.learnspigot.bot.Server.isPluginDev
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SuggestionListener : ListenerAdapter() {

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.author.isBot || !e.isPluginDev || !Server.CHANNEL_SUGGESTIONS.isChannel(e.channel)) return

        e.message.apply {
            addReaction(Server.EMOJI_UPVOTE).queue()
            addReaction(Server.EMOJI_DOWNVOTE).queue()
            createThreadChannel("Suggestion from ${e.author.effectiveName}").queue()
        }
    }

}