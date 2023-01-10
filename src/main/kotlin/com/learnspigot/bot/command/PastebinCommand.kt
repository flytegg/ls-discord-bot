package com.learnspigot.bot.command

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

class PastebinCommand(guild: Guild, bot: JDA) {
    init {
        guild.upsertCommand("pastebin", "Link the LearnSpigot Pastebin") {
            restrict(guild = true)
            bot.onCommand("pastebin") {
                it.reply("LearnSpigot Pastebin: https://paste.learnspigot.com/").queue()
            }
        }.queue()
    }
}