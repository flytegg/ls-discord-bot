package com.learnspigot.bot.workshop

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
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

        println(threads.size)

        if (threads.size >= 2) {
            event.channel.delete().queue()
            owner.user.openPrivateChannel().queue {
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
            return
        }
        workShopPostRegistry.posts[thread.id] = owner.id
    }
}