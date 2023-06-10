package com.learnspigot.bot.starboard

import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.react.*
import net.dv8tion.jda.api.hooks.ListenerAdapter

class StarboardListener : ListenerAdapter() {

    @Inject
    private lateinit var starboardRegistry: StarboardRegistry

    private fun isStarOrNostarboardEmoji(emoji: EmojiUnion): Boolean =
        StarboardUtil.isStarEmoji(emoji) || StarboardUtil.isNostarboardEmoji(emoji)

    override fun onMessageReactionRemoveEmoji(event: MessageReactionRemoveEmojiEvent) {
        if (!event.isFromGuild) return
        if (!isStarOrNostarboardEmoji(event.emoji)) return

        val message = event.channel.retrieveMessageById(event.messageId).complete()
        starboardRegistry.updateStarboard(message, 0)

    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild) return
        if (!isStarOrNostarboardEmoji(event.emoji)) return

        val message = event.retrieveMessage().complete()
        starboardRegistry.updateStarboard(message)
    }

    override fun onMessageReactionRemove(event: MessageReactionRemoveEvent) {
        if (!event.isFromGuild) return
        if (!isStarOrNostarboardEmoji(event.emoji)) return

        val message = event.retrieveMessage().complete()
        starboardRegistry.updateStarboard(message)
    }

    override fun onMessageReactionRemoveAll(event: MessageReactionRemoveAllEvent) {
        if (!event.isFromGuild) return
        val message = event.channel.retrieveMessageById(event.messageId).complete()
        starboardRegistry.updateStarboard(message)
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        starboardRegistry.removeStarboardEntryAndMessageIfExists(event.messageId)
    }

    override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
        event.messageIds.forEach(starboardRegistry::removeStarboardEntryAndMessageIfExists)
    }
}