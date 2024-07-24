package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Environment
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class VCListener : ListenerAdapter() {
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent){
        val guild = event.guild
        val voiceChannel = guild.getVoiceChannelById(Environment.VOICE_CHANNEL_ID)
        val leftChannel = event.channelLeft

        if (leftChannel != null && leftChannel.members.isEmpty()) {
            if ((leftChannel == voiceChannel) || (leftChannel is StageChannel) || (leftChannel.parentCategoryId != Environment.VOICE_CHANNEL_ID)) return
            leftChannel.delete().queue()
        }
    }
}