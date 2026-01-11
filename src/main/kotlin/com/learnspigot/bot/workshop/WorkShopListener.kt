package com.learnspigot.bot.workshop

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class WorkShopListener: ListenerAdapter() {

    @Inject
    lateinit var workShopPostRegistry: WorkShopPostRegistry

    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val thread = event.channel.asThreadChannel()
        if (thread.parentChannel.id != Server.workshopChannel.id) return
        val owner = thread.owner ?: return

        val threads = workShopPostRegistry.posts
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
        workShopPostRegistry.posts[thread.id] = owner.id
    }

    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val channelId = e.channel.asThreadChannel().parentChannel.id
        if (channelId != Server.workshopChannel.id) return
        val channel = e.channel.asThreadChannel()

        channel.history.retrievePast(1).queue { messages ->
            val message = messages.last()
            if (channel.isArchived && !message.author.isBot && message.embeds.isNotEmpty() && !message.embeds.any { it.title?.contains("workshop", ignoreCase = true) == true }) { // Penguin, You have any better idea than checking history for last message?
                channel.manager.setArchived(false).setLocked(false).queue()
            }
        }
    }
}