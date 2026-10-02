package com.learnspigot.bot.help.stats

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateLockedEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class HelpStatsListener : ListenerAdapter() {

    private val ThreadChannel.isHelpPost get() = parentChannel.idLong == Server.CHANNEL_HELP.idLong

    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel().takeIf { it.isHelpPost } ?: return
        Registry.HELP_STATS.postCreated(thread)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel().takeIf { it.isHelpPost } ?: return
        // The starter message is already counted when the post is created
        if (event.messageIdLong == thread.idLong) return
        Registry.HELP_STATS.messageSent(thread, event.author.id, event.author.isBot || event.author.isSystem, event.message.timeCreated.toEpochSecond())
    }

    override fun onChannelUpdateArchived(event: ChannelUpdateArchivedEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel().takeIf { it.isHelpPost } ?: return
        Registry.HELP_STATS.sync(thread)
    }

    override fun onChannelUpdateLocked(event: ChannelUpdateLockedEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel().takeIf { it.isHelpPost } ?: return
        Registry.HELP_STATS.sync(thread)
    }

    override fun onChannelDelete(event: ChannelDeleteEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        Registry.HELP_STATS.postDeleted(event.channel.id)
    }

}
