package com.learnspigot.bot.starboard

import com.learnspigot.bot.Server
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.events.message.react.*
import net.dv8tion.jda.api.hooks.ListenerAdapter

class StarboardListener : ListenerAdapter() {

    @Inject
    private lateinit var starboardRegistry: StarboardRegistry

    private fun getMessage(messageId: String, channel: MessageChannelUnion): Message {
        return channel.retrieveMessageById(messageId).complete()
    }

    override fun onMessageReactionRemoveEmoji(event: MessageReactionRemoveEmojiEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.starboardChannel) return

        println("onMessageReactionRemoveEmoji in guild and not in starboard channel")

        when (event.emoji) {
            Server.starEmoji -> starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel), 0)
            Server.nostarboardEmoji -> starboardRegistry.updateNostarboard(getMessage(event.messageId, event.channel))
        }

    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.starboardChannel) return
        when (event.emoji) {
            Server.starEmoji -> starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel))
            Server.nostarboardEmoji -> starboardRegistry.updateNostarboard(getMessage(event.messageId, event.channel))
        }
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        println("onMessageReactionRemove")
        if (!event.isFromGuild) return
        if (event.channel == Server.starboardChannel) return
        println("onMessageReactionRemove in guild and not in starboard channel")
        when (event.emoji) {
            Server.starEmoji -> starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel))
            Server.nostarboardEmoji -> starboardRegistry.updateNostarboard(getMessage(event.messageId, event.channel))
        }
    }

    override fun onMessageReactionRemoveAll(event: MessageReactionRemoveAllEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.starboardChannel) return

        starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel), 0)
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (event.channel == Server.starboardChannel) return

        starboardRegistry.removeStarboardEntryAndMessageIfExists(event.messageId)
    }

    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
        if (event.channel == Server.starboardChannel) return

        event.messageIds.forEach(starboardRegistry::removeStarboardEntryAndMessageIfExists)
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!event.isFromGuild) return
        if (event.channel == Server.starboardChannel) return

        starboardRegistry.updateStarboard(getMessage(event.messageId, event.channel), true)
    }
}