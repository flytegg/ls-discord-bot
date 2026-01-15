package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ContextException
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class VCCommand {

    private val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    @Command("createvoice")
    @Description("Create a temporary voice channel!")
    @CommandPermission(Permission.CREATE_PUBLIC_THREADS)
    fun onCreateVoiceCommand(
        actor: SlashCommandActor,
        @Description("Max user limit") @Optional limit: Int?,
    ) {
        val event = actor.commandEvent()
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
