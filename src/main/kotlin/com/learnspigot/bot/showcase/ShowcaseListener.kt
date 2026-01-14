package com.learnspigot.bot.showcase

import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isPluginDev
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ShowcaseListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isPluginDev || !Server.CHANNEL_SHOWCASE.isChannel(event.channel)) return

        event.message.addReaction(Emoji.fromUnicode("❤️")).queue()
        event.message.createThreadChannel("Showcase from ${event.author.effectiveName}").queue()
    }
}