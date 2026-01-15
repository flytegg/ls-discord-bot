package com.learnspigot.bot.notice

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Suggest
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.Choices
import revxrsal.commands.jda.annotation.CommandPermission

class NoticeCommand {

    @Command(
        "noticelist"
    )
    @Description("List the available notices.")
    fun onNoticeListCommand(actor: SlashCommandActor) {
        val event = actor.commandEvent()
        event.replyEmbeds(listNoticesEmbed("Available notices")).setEphemeral(true).queue()
    }

    @Command(
        "notice"
    )
    @Description("Send a notice to an OP")
    fun onNoticeCommand(
        actor: SlashCommandActor,
        @Description("Notice name") @Choices("help", "close", "ping") key: String,
        @Optional @Description("The user this notice is targeted to") @Named("target-user") targetUser: User?,
    ) {
        val event = actor.commandEvent()
        val notice = Notice.entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
            ?: return event.replyEmbeds(listNoticesEmbed("Could not find notice.", "Available notices:")).setEphemeral(true).queue()

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

    private fun listNoticesEmbed(title: String, description: String? = null): MessageEmbed {
        val embed = embed().setTitle(title)
        if (description != null) embed.setDescription(description)

        for ((i, notice) in Notice.entries.withIndex()) {
            val noticeName = notice.name.lowercase().replaceFirstChar { it.uppercase() }
            embed.addField("${i + 1}. $noticeName", notice.description, false)
        }

        return embed.build()
    }

}