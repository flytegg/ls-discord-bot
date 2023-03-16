package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.editEmbed
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

class PasteCommand(guild: Guild, bot: JDA) {

    init {
        guild.upsertCommand("paste", "Send the link for the LearnSpigot Paste service") {
            restrict(guild = true)
            bot.onCommand("paste") {
                it.deferReply().queue()
                it.editEmbed({
                    title = "LearnSpigot Paste"
                    color = EMBED_COLOR
                    description = "We recommend using our own paste service: https://paste.learnspigot.com"
                }).queue()
            }
        }.queue()
    }
}