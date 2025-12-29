package com.learnspigot.bot.workshop

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class WorkShopListener: ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel()
        if (thread.parentChannel.id != Server.workshopChannel.id) return

        val threads = (thread.parentChannel.threadChannels + thread.parentChannel.retrieveArchivedPublicThreadChannels()).filter {
            it.owner == thread.owner && it.id != thread.id
        }.distinct()

        if (threads.size >= 2) {
            event.channel.delete().queue()
            thread.owner!!.user.openPrivateChannel().queue {
                it.sendMessageEmbeds(
                    embed()
                        .setAuthor("You cannot create more than two workshop threads!")
                        .setTitle("Please delete one of the following to create a new one:")
                        .setDescription("""
                            ${threads[threads.size - 1].asMention}
                            ${threads[threads.size - 2].asMention}
                            """.trimIndent())
                        .build()
                ).queue()
            }
        }
    }
}