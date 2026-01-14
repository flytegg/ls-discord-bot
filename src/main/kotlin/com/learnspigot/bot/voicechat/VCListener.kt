package com.learnspigot.bot.voicechat

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class VCListener: ListenerAdapter() {
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val leftChannel = event.channelLeft?.takeIf { it.members.isEmpty() } ?: return

        // If channel is our normal voice channel, is a Stage Channel or is not in the chat category, don't delete it because it's not a temp channel.
        if (Server.CHANNEL_VOICE.isChannel(leftChannel) || (leftChannel is StageChannel) || (leftChannel.parentCategoryIdLong != Server.CATEGORY_CHAT.idLong)) return
        leftChannel.delete().queue()
    }
}