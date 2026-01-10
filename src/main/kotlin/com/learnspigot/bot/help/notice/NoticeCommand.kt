package com.learnspigot.bot.help.notice

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class NoticeCommand {

    @Command(
        name = "notice",
        description = "Send a notice to an OP",
        permissions = [Permission.MANAGE_EMOJIS_AND_STICKERS]
    )
    fun onNoticeCommand(
        event: SlashCommandInteractionEvent,
        @Description("Notice name") key: String,
        @Description("The user this notice is targeted to") targetUser: User,
    ) {
        val notice = Notice.entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: return run {
            val embed = embed().setTitle("Could not find notice.").setDescription("Available notices:")

            for ((i, notice) in Notice.entries.withIndex()) {
                val noticeName = notice.name.lowercase().replaceFirstChar { it.uppercase() }
                embed.addField("${i + 1}. $noticeName", notice.rawMessage(), false)
            }
            event.replyEmbeds(embed.build()).setEphemeral(true).queue()
        }

        if (notice.helpPostOnly) {
            if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD)
                return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

            val channel = event.guildChannel.asThreadChannel()
            if (channel.parentChannel.id != Server.helpChannel.id)
                return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
        }

        val message = notice.message(targetUser.idLong)
        event.reply(message).queue()
    }

}