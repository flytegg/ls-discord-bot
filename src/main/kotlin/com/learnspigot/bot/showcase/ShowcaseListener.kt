package com.learnspigot.bot.showcase

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ShowcaseListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!event.isFromGuild) return
        if (event.guild.id != Server.guildId) return
        if (event.channel.id != Environment.get("SHOWCASE_CHANNEL_ID")) return

        event.message.addReaction(Emoji.fromUnicode("❤️")).queue()
        event.message.createThreadChannel("Showcase from ${event.message.author.globalName}").queue()
    }
}