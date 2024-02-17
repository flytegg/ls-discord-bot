package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Environment
import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.entities.Widget.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class VCListener : ListenerAdapter() {
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent){
        val guild = event.guild
        val voiceChannel = guild.getVoiceChannelById(Environment.get("VOICE_CHANNEL_ID"))
        val joinedChannel = event.channelJoined
        val leftChannel = event.channelLeft
        val oldChannel = event.oldValue

        if (joinedChannel !is VoiceChannel || joinedChannel.parentCategoryId != Environment.get("CHAT_CATEGORY")) return
        if (joinedChannel == voiceChannel){

            if ((oldChannel != null) && oldChannel.members.isEmpty()){
                oldChannel.delete().queue()
            }

            val newChannel = guild.createVoiceChannel("${event.member.effectiveName}'s channel", joinedChannel.parentCategory!!).complete()
            guild.moveVoiceMember(event.member, newChannel).queue()
            return
        }

        if (leftChannel != null){
            if (leftChannel.members.isEmpty() && (leftChannel != voiceChannel)){
                leftChannel.delete().queue()
            }
        }

    }
}