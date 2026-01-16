package com.learnspigot.bot.notice

import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isStaff
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class NoticeListener : ListenerAdapter() {

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        val notice = when(event.name) {
            "Help Notice" -> Notice.HELP
            "Close Notice" -> Notice.CLOSE
            "Ping Notice" -> Notice.PING
            else ->  null
        } ?: return

        if (notice.helpPostOnly) {
            if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD)
                return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

            val channel = event.guildChannel.asThreadChannel()
            if (channel.parentChannel.id != Server.CHANNEL_HELP.id)
                return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

            val reply = event.deferReply()
            notice.reply(reply, channel.owner!!.idLong)
            reply.queue()
        } else {
            if (targetUser == null) return event.reply("Please provide a target user.").setEphemeral(true).queue()

            val reply = event.deferReply()
            notice.reply(reply, targetUser.idLong)
            reply.queue()
        }

    }

}