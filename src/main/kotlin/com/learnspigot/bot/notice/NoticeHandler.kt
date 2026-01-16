package com.learnspigot.bot.notice

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

object NoticeHandler {

    fun handle(notice: Notice, event: IReplyCallback) {

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