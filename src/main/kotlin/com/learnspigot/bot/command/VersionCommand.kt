package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import java.io.BufferedReader
import java.io.InputStreamReader

class VersionCommand(guild: Guild, bot: JDA) {

    init {
        guild.upsertCommand("version", "Check the version of the bot") {
            restrict(guild = true)
            bot.onCommand("version") {
                val process = withContext(Dispatchers.IO) {
                    ProcessBuilder("git", "rev-parse", "HEAD").start()
                }
                val version = BufferedReader(InputStreamReader(process.inputStream)).readLines()[0]
                it.replyEmbed({
                    title = "Current Version"
                    url = "https://github.com/learnspigot/discord-bot/commit/${version}"
                    color = EMBED_COLOR
                    description = "The bot is currently running **${version.take(8)}**."
                }).queue()
            }
        }.queue()
    }
}