package com.learnspigot.bot.workshop

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class WorkShopListener: ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel()
        if (thread.parentChannel.id != Server.workshopChannel.id) return

        val existing = thread.parentChannel.threadChannels.filter {
            it.owner == thread.owner && it.id != thread.id
        }

        if (existing.isNotEmpty()) {
            event.channel.delete().queue()
            thread.owner!!.user.openPrivateChannel().queue {
                it.sendMessage("You cannot create a channel as you already have one!\n${existing[existing.size - 1].asMention}").queue()
            }
        }
    }
}