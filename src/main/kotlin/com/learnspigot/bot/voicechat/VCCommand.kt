package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Environment
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class VCCommand {

  @Command(
      name = "createvoice",
      description = "Create a temporary voice channel!",
      permissions = [Permission.CREATE_PUBLIC_THREADS])
  fun onCreateVoiceCommand(
      event: SlashCommandInteractionEvent,
      @Description("Max user limit") @Optional limit: Int?,
  ) {
      if (event.member!!.voiceState?.inAudioChannel() != true){
          event.reply("You must be in a voice channel to use this command").setEphemeral(true).queue()
          return
      }

      val guild = event.guild ?: return
      val newChannel = guild.createVoiceChannel("${event.member!!.effectiveName}'s channel", event.guild!!.getCategoryById(Environment.get("CHAT_CATEGORY"))).complete()

      if (limit != null) {
          newChannel.manager.setUserLimit(limit).queue()
      }

      guild.moveVoiceMember(event.member!!, newChannel).queue()
      event.reply("VC created, enjoy!").setEphemeral(true).queue()
  }
}
