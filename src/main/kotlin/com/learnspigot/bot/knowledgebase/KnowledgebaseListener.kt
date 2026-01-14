package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isChannel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class KnowledgebaseListener : ListenerAdapter() {

    private inline val knowledgebasePostRegistry: KnowledgebasePostRegistry get() = Registry.KNOWLEDGEBASE

    val ChannelUnion.isInKnowledgebase: Boolean get() = asThreadChannel().parentChannel.isChannel(Server.CHANNEL_KNOWLEDGEBASE)

    override fun onChannelCreate(e: ChannelCreateEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (!e.channel.isInKnowledgebase) return

        knowledgebasePostRegistry.posts[e.channel.name] = e.channel.id
    }

    override fun onChannelDelete(e: ChannelDeleteEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (!e.channel.isInKnowledgebase) return

        knowledgebasePostRegistry.posts.remove(e.channel.name)
    }

    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (!e.channel.isInKnowledgebase && !e.channel.asThreadChannel().parentChannel.isChannel(Server.CHANNEL_PROJECTS)) return

        val channel = e.channel.asThreadChannel()
        if (channel.isArchived){
            channel.manager.setArchived(false).setLocked(false).queue()
        }
    }

}