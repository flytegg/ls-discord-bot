package com.learnspigot.bot.util

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ForumKeepAlive: ListenerAdapter() {

    private val forums = listOf(Server.CHANNEL_PROJECTS, Server.CHANNEL_KNOWLEDGEBASE, Server.CHANNEL_WORKSHOP)

    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val channel = e.channel.asThreadChannel()

        // Check this is a post from a forum channel we are keeping alive
        if (forums.none { it.idLong == channel.parentChannel.idLong }) return

        if (channel.isArchived) {
            // Don't keep-alive this channel if we are trying to close it.
            if (Server.CHANNEL_WORKSHOP.isChannel(channel.parentChannel) && Registry.WORKSHOP.channelsMarkedForClosing.contains(channel.idLong)) return;

            channel.manager.setArchived(false).setLocked(false).queue()
        }
    }

}