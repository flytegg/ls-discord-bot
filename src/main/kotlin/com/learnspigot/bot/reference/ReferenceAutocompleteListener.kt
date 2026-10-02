package com.learnspigot.bot.reference

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

// TODO Lamp suggestion providers don't support name-value pairs, yet.
class ReferenceAutocompleteListener : ListenerAdapter() {
    companion object {
        lateinit var command: ReferenceCommand
    }

    val referenceEntryProvider = ReferenceEntryProvider()
    val mappingProvider = MappingProvider()

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        when (event.focusedOption.name) {
            "entry" -> {
                event
                    .replyChoices(
                        referenceEntryProvider.suggest(
                            event.focusedOption.value,
                            event.getOption("version")?.asString,
                        ),
                    ).queue()
            }

            "version" -> {
                event
                    .replyChoiceStrings(mappingProvider.getSuggestions(event.focusedOption.value))
                    .queue()
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "reference") return
        when (event.subcommandName) {
            "nms" -> {
                command.onReferenceNms(
                    event,
                    event.getOption("version")!!.asString,
                    event.getOption("entry")!!.asString,
                )
            }

            "class" -> {
                command.onReference(
                    event,
                    event.getOption("entry")!!.asString,
                )
            }
        }
    }
}
