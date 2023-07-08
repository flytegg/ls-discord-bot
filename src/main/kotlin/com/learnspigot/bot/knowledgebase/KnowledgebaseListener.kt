package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class KnowledgebaseListener : ListenerAdapter() {

    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.guild.id != Server.guildId) return
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (e.channel.asThreadChannel().parentChannel.id != Environment.get("KNOWLEDGEBASE_CHANNEL_ID") && e.channel.asThreadChannel().parentChannel.id != Environment.get("PROJECTS_CHANNEL_ID")) return

        val channel = e.channel.asThreadChannel()
        if (channel.isArchived){
            channel.manager.setArchived(false).setLocked(false).queue()
        }
    }

}