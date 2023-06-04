package com.learnspigot.bot.knowledgebase

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class KnowledgebaseListener : ListenerAdapter() {

    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.guild.id != System.getenv("GUILD_ID")) return
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (e.channel.asThreadChannel().parentChannel.id != System.getenv("KNOWLEDGEBASE_CHANNEL_ID") && e.channel.asThreadChannel().parentChannel.id != System.getenv("PROJECTS_CHANNEL_ID")) return

        val channel = e.channel.asThreadChannel()
        if (channel.isArchived){
            channel.manager.setArchived(false).setLocked(false).queue()
        }
    }

}