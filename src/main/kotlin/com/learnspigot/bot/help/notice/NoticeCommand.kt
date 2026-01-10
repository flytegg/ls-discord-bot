package com.learnspigot.bot.help.notice

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class NoticeCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Inject
    private lateinit var noticeRegistry: NoticeRegistry

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
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD)
            return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != Server.helpChannel.id)
            return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()

        val notice = noticeRegistry.getNotice(key, mapOf("user_id" to targetUser.id)) ?: return run {
            val embed = embed().setTitle("Could not find notice.").setDescription("Available notices:")

            for ((i, notice) in noticeRegistry.notices().toList().withIndex()) embed.addField("${i + 1}. ${notice.first}", notice.second, false)
            event.replyEmbeds(embed.build()).setEphemeral(true).queue()
        }

        event.replyEmbeds(
            embed()
                .setTitle("${key.replaceFirstChar { it.uppercase() }} notice")
                .setDescription(notice)
                .build())
            .queue()
    }

}