package com.learnspigot.bot.util

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import revxrsal.commands.Lamp
import revxrsal.commands.jda.actor.SlashActorFactory
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.slash.JDASlashListener

// TODO remove this when Lamp adds name-value pair support for suggestions
class BlockingJDAListener<A : SlashCommandActor>(
    lamp: Lamp<A>,
    factory: SlashActorFactory<A>,
) : ListenerAdapter() {
    val handle = JDASlashListener(lamp, factory)

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (event.name == "reference") return
        handle.onEvent(event)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "reference") return
        handle.onEvent(event)
    }
}
