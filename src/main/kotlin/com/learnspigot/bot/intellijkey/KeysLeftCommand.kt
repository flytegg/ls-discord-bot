package com.learnspigot.bot.intellijkey

import com.learnspigot.bot.Registry
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class KeysLeftCommand {

    @Command(
        name = "keysleft",
        description = "View amount of IntelliJ Ultimate keys remaining",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onKeysLeftCommand(event: SlashCommandInteractionEvent) {
        val amount = Registry.IJ_ULTIMATE_KEYS.keys.size
        event.reply("There are **$amount** IntelliJ Ultimate keys left in this batch." + if (amount < 100) " Maybe time to restock?" else "").setEphemeral(true).queue()
    }

}