package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Environment
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
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
        val newChannel = guild.createVoiceChannel(
            "${event.member!!.effectiveName}'s channel",
            event.guild!!.getCategoryById(Environment.get("CHAT_CATEGORY"))
        ).complete()

        if (limit != null) {
            newChannel.manager.setUserLimit(limit).queue()
        }

        if (event.member!!.voiceState?.inAudioChannel() == true)
            guild.moveVoiceMember(event.member!!, newChannel).queue()

        event.reply("Your voice channel has been created - ${newChannel.asMention}").setEphemeral(true).queue()

        scheduledExecutor.schedule({
            if (newChannel == null) return@schedule
            if (newChannel.members.isEmpty()) newChannel.delete().queue()
        }, 5, TimeUnit.MINUTES)
    }

}
