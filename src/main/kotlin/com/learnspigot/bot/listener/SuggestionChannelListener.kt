package com.learnspigot.bot.listener

import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class SuggestionChannelListener(bot: JDA) {
    init {
        bot.listener<MessageReceivedEvent> {
            if(it.channel.id != System.getenv("SUGGESTIONS_CHANNEL_ID")) return@listener
            if(it.author.isBot) return@listener
            it.message.delete().queue()
        }
    }

}