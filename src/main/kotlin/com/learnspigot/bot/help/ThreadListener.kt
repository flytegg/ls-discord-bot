package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class ThreadListener: ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val channel = event.channel.asThreadChannel()
        if (channel.parentChannel.id != Server.CHANNEL_HELP.id && channel.parentChannel.id != Server.CHANNEL_CODE_REVIEW.id) return

        val closeId = Server.closeCommandId

        val embed = embed().setTitle("Thank you for creating a post!")
        embed.setDescription(
            if (channel.parentChannel.id == Server.CHANNEL_HELP.id) {
                """
                    Please allow someone to read through your post and answer it!
                    
                    If you fixed your problem, please run ${if (closeId == null) "/close" else "</close:$closeId>"}.
                """
            } else {
                """
                Please allow someone to read through your code and give feedback!
                    
                If you have gotten useful feedback, please run ${if (closeId == null) "/close" else "</close:$closeId>"}.
            """
            }.trimIndent()
        )
        sendWelcome(channel, embed.build(), 3)
    }

    // Forum posts reject messages until the author's starter message exists (40058), so retry briefly
    private fun sendWelcome(channel: ThreadChannel, embed: MessageEmbed, attemptsLeft: Int) {
        channel.sendMessageEmbeds(embed).queueAfter(if (attemptsLeft == 3) 0 else 2, TimeUnit.SECONDS, null) { e ->
            if (e is ErrorResponseException && e.errorCode == 40058 && attemptsLeft > 1) {
                sendWelcome(channel, embed, attemptsLeft - 1)
            } else {
                println("Failed to send welcome message in ${channel.id}: ${e.message}")
            }
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return

        val channel = event.channel.asThreadChannel()
        if (channel.parentChannel.id != Server.CHANNEL_HELP.id && channel.parentChannel.id != Server.CHANNEL_CODE_REVIEW.id) return
        // This checks if the message being reacted to is the pilot message in the thread
        if (channel.idLong != event.messageIdLong) return

        val member = event.member
        event.retrieveUser().queue { user ->
            if (user.isBot || user.isSystem || member.isManager) return@queue
            event.reaction.removeReaction(user).queue()
        }
    }

}