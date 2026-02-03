package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands.user

class ThreadListener: ListenerAdapter() {
    override fun onChannelCreate(event: ChannelCreateEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        val channel = event.channel.asThreadChannel()
        if (channel.parentChannel.id != Server.CHANNEL_HELP.id && channel.parentChannel.id != Server.CHANNEL_CODE_REVIEW.id) return

        val closeId = event.guild!!.retrieveCommands().complete()
            .firstOrNull { it.name == "close" }
            ?.id

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
        event.channel.asThreadChannel().sendMessageEmbeds(embed.build()).queue()
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