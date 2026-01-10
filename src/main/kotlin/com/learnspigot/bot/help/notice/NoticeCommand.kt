package com.learnspigot.bot.help.notice

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class NoticeCommand {

    @Command(
        name = "noticelist",
        description = "List the available notices.",
        permissions = [Permission.MANAGE_EMOJIS_AND_STICKERS]
    )
    fun onNoticeListCommand(event: SlashCommandInteractionEvent) {
        event.replyEmbeds(listNoticesEmbed("Available notices")).setEphemeral(true).queue()
    }

    @Command(
        name = "notice",
        description = "Send a notice to an OP",
        permissions = [Permission.MANAGE_EMOJIS_AND_STICKERS]
    )
    fun onNoticeCommand(
        event: SlashCommandInteractionEvent,
        @Description("Notice name") key: String,
        @Optional @Description("The user this notice is targeted to") targetUser: User?,
    ) {
        val notice = Notice.entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
            ?: return event.replyEmbeds(listNoticesEmbed("Could not find notice.", "Available notices:")).setEphemeral(true).queue()

        if (notice.helpPostOnly) {
            if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD)
                return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

            val channel = event.guildChannel.asThreadChannel()
            if (channel.parentChannel.id != Server.helpChannel.id)
                return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

            val message = notice.message(channel.owner!!.idLong)
            event.reply(message).queue()
        } else {
            if (targetUser == null) return event.reply("Please provide a target user.").setEphemeral(true).queue()

            val message = notice.message(targetUser.idLong)
            event.reply(message).queue()
        }
    }

    private fun listNoticesEmbed(title: String, description: String? = null): MessageEmbed {
        val embed = embed().setTitle(title)
        if (description != null) embed.setDescription(description)

        for ((i, notice) in Notice.entries.withIndex()) {
            val noticeName = notice.name.lowercase().replaceFirstChar { it.uppercase() }
            embed.addField("${i + 1}. $noticeName", notice.rawMessage(), false)
        }

        return embed.build()
    }

}