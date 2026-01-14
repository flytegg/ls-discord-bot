package com.learnspigot.bot.starboard

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class StarboardListener : ListenerAdapter() {

    private inline val starboardRegistry: StarboardRegistry get() = Registry.STARBOARD

    private fun getMessage(messageId: String, channel: MessageChannelUnion): Message {
        return channel.retrieveMessageById(messageId).complete()
    }

    override fun onMessageReactionRemoveEmoji(event: MessageReactionRemoveEmojiEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.CHANNEL_STARBOARD) return

        when (event.emoji) {
            Server.EMOJI_STAR -> starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel), 0)
            Server.EMOJI_NO_STARBOARD -> starboardRegistry.updateNoStarboard(getMessage(event.messageId, event.channel))
        }
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.CHANNEL_STARBOARD) return

        when (event.emoji) {
            Server.EMOJI_STAR -> starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel))
            Server.EMOJI_NO_STARBOARD -> starboardRegistry.updateNoStarboard(getMessage(event.messageId, event.channel))
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.CHANNEL_STARBOARD) return

        when (event.emoji) {
            Server.EMOJI_STAR -> starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel))
            Server.EMOJI_NO_STARBOARD -> starboardRegistry.updateNoStarboard(getMessage(event.messageId, event.channel))
        }
    }

    override fun onMessageReactionRemoveAll(event: MessageReactionRemoveAllEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.CHANNEL_STARBOARD) return

        starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel), 0)
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.channel == Server.CHANNEL_STARBOARD) return

        starboardRegistry.removeStarboardEntry(event.messageId)
    }

    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
        if (event.channel == Server.CHANNEL_STARBOARD) return

        event.messageIds.forEach(starboardRegistry::removeStarboardEntry)
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.CHANNEL_STARBOARD) return

        runCatching {
            starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel), true)
        }
    }
}