package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.http.HastebinService
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

class HastebinCommand(guild: Guild, bot: JDA) {

    init {
        guild.upsertCommand("hastebin", "Realtime stock price of Tesla") {
            restrict(guild = true)
            option<String>(name = "data", description = "What data to create hastebin with")
            bot.onCommand("hastebin") {
                val data = it.getOption("data")!!.asString
                val url = HastebinService().createDocument(data).getUrl()
                it.replyEmbed({
                    title = "Created Hastebin"
                    description = "You can view the hastebin here: ${url}"
                    color = EMBED_COLOR
                }).queue()
            }
        }.queue()
    }
}