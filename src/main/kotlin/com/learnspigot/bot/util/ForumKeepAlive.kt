package com.learnspigot.bot.util

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ForumKeepAlive: ListenerAdapter() {

    private val forums = listOf(Server.CHANNEL_PROJECTS, Server.CHANNEL_KNOWLEDGEBASE)

    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val channel = e.channel.asThreadChannel()

        if (forums.none { it.idLong == channel.parentChannel.idLong }) return

        if (channel.isArchived){
            channel.manager.setArchived(false).setLocked(false).queue()
        }
    }

}