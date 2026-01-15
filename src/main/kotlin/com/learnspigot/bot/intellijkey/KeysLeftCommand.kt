package com.learnspigot.bot.intellijkey

import com.learnspigot.bot.Registry
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class KeysLeftCommand {

    @Command("keysleft")
    @Description("View amount of IntelliJ Ultimate keys remaining")
    @CommandPermission(Permission.MANAGE_ROLES)
    fun onKeysLeftCommand(actor: SlashCommandActor) {
        val event = actor.commandEvent()
        val amount = Registry.IJ_ULTIMATE_KEYS.keys.size
        event.reply("There are **$amount** IntelliJ Ultimate keys left in this batch." + if (amount < 100) " Maybe time to restock?" else "").setEphemeral(true).queue()
    }

}