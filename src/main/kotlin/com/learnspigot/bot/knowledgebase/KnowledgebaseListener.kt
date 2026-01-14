package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class KnowledgebaseListener : ListenerAdapter() {

    val ChannelUnion.isInKnowledgebase: Boolean get() = asThreadChannel().parentChannel.isChannel(Server.CHANNEL_KNOWLEDGEBASE)

    override fun onChannelCreate(e: ChannelCreateEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (!e.channel.isInKnowledgebase) return

        Registry.KNOWLEDGEBASE.posts[e.channel.name] = e.channel.id
    }

    override fun onChannelDelete(e: ChannelDeleteEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (!e.channel.isInKnowledgebase) return

        Registry.KNOWLEDGEBASE.posts.remove(e.channel.name)
    }

}