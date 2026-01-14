package com.learnspigot.bot.workshop

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class WorkShopListener: ListenerAdapter() {

    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel()
        if (thread.parentChannel.id != Server.CHANNEL_WORKSHOP.id) return
        val owner = thread.owner ?: return

        val threads = Registry.WORKSHOP.posts
            .filter { it.value == owner.id }
            .mapNotNull { event.guild.getThreadChannelById(it.key) }

        if (threads.size >= 2) {
            event.channel.delete().queue()

            val messageThreads = StringBuilder()
            threads.forEach { post: ThreadChannel ->
                messageThreads.append("- ").append(post.asMention).append("\n")
            }

            owner.user.openPrivateChannel().queue {
                it.sendMessageEmbeds(embed()
                    .setTitle("Unable to create another workshop!")
                    .setDescription("""
                        You currently have 2 or more workshops open. Please close one of the workshops below before making another one.
                    """.trimIndent())
                    .addField("Open posts", messageThreads.toString(), false).build()
                ).queue()
            }
            return
        }
        Registry.WORKSHOP.posts[thread.id] = owner.id
    }
}