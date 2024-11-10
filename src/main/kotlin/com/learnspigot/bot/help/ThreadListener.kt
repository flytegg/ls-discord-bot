package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.PasteBins
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ThreadListener : ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (event.channel.asThreadChannel().parentChannel.id != Server.helpChannel.id) return

        val threadChannel = event.channel.asThreadChannel()
        val content = if (threadChannel.parentChannel.type != ChannelType.FORUM) {
            threadChannel.retrieveParentMessage().complete()?.contentRaw
        } else {
            threadChannel.retrieveStartMessage().complete()?.contentRaw
        }

        val KNOWN_PASTEBINS = listOf(
            PasteBins.ls,
            PasteBins.pb,
            PasteBins.md5,
            PasteBins.discord,
            PasteBins.helpch
        )

        val closeId = event.guild!!.retrieveCommands().complete()
            .firstOrNull { it.name == "close" }
            ?.id
        val containsPastebinLink = KNOWN_PASTEBINS.any { content?.contains(it, ignoreCase = true) == true }

        threadChannel.sendMessageEmbeds(
            embed()
                .setTitle("Thank you for creating a post!")
                .setDescription("""
                    Please allow someone to read through your post and answer it!
                    
                    If you fixed your problem, please run ${if (closeId == null) "/close" else "</close:$closeId>"}.
                """.trimIndent())
                .build()
        ).queue()

        if (!containsPastebinLink) {
            threadChannel.sendMessageEmbeds(
                embed()
                    .setTitle("No Code Provided!")
                    .setDescription(
                        """
                        We've noticed that you didn't send us any code, if that's voluntary, you can ignore this message!
                        If not, feel free to send us the code with our pastebin: https://paste.learnspigot.com
                    """.trimIndent()
                    )
                    .build()
            ).queue()
        }
    }
}