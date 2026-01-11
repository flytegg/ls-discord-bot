package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Server
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ContextException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class VCCommand {

    private val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    @Command(
        name = "createvoice",
        description = "Create a temporary voice channel!",
        permissions = [Permission.CREATE_PUBLIC_THREADS]
    )
    fun onCreateVoiceCommand(
        event: SlashCommandInteractionEvent,
        @Description("Max user limit") @Optional limit: Int?,
    ) {
        val guild = event.guild ?: return
        val member = event.member ?: return

        if (guild.getVoiceChannelsByName("${member.effectiveName}'s channel", true).isNotEmpty()) {
            return event.reply("You already have a voice channel!").setEphemeral(true).queue()
        }

        if (limit != null && limit < 1) {
            return event.reply("The max user limit must be 1 or higher.").setEphemeral(true).queue()
        }

        val newChannel = guild.createVoiceChannel("${member.effectiveName}'s channel", Server.CATEGORY_CHAT).complete()

        if (limit != null) {
            newChannel.manager.setUserLimit(limit).queue()
        }

        if (member.voiceState?.inAudioChannel() == true)
            guild.moveVoiceMember(member, newChannel).queue()

        event.reply("Your voice channel has been created - ${newChannel.asMention}").setEphemeral(true).queue()

        scheduledExecutor.schedule({
            try {
                if (newChannel.members.isEmpty()) newChannel.delete().queue()
            } catch (_: ContextException) {}
        }, 5, TimeUnit.MINUTES)
    }

}
