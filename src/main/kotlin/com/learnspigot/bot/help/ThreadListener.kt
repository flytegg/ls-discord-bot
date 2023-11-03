package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ThreadListener : ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (event.channel.asThreadChannel().parentChannel.id != Server.helpChannel.id) return

        val closeId = event.guild!!.retrieveCommands().complete()
            .firstOrNull { it.name == "close" }
            ?.id

        event.channel.asThreadChannel().sendMessageEmbeds(
            embed()
                .setTitle("Thank you for creating a post!")
                .setDescription("""
                    Please allow someone to read through your post and answer it!
                    
                    If you fixed your problem, please run ${if (closeId == null) "/close" else "</close:$closeId>"}.
                """.trimIndent())
                .build()
        ).queue()
    }
}
