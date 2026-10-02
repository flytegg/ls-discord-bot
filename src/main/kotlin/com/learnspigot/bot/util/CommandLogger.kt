package com.learnspigot.bot.util

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Logs every slash and context menu command to console, so we can always see who ran what.
 */
class CommandLogger : ListenerAdapter() {
    override fun onGenericCommandInteraction(event: GenericCommandInteractionEvent) {
        val target = when (event) {
            is MessageContextInteractionEvent -> " on message ${event.target.id} by '${event.target.author.name}'"
            is UserContextInteractionEvent -> " on user '${event.target.name}' (${event.target.id})"
            else -> ""
        }
        val channel = event.channel?.let { "#${it.name} (${it.id})" } ?: event.channelId
        println("[Command] '${event.user.name}' (${event.user.id}) ran ${event.commandString}$target in $channel")
    }
}
