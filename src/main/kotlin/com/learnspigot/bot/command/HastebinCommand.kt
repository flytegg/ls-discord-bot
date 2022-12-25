package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.http.FinanceService
import com.learnspigot.bot.http.HastebinService
import com.learnspigot.bot.http.HttpService
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HastebinCommand(guild: Guild, bot: JDA) {

    init {
        guild.upsertCommand("hastebin", "Realtime stock price of Tesla") {
            restrict(guild = true)
            option<String>(name = "data", description = "What data to create hastebin with")
            bot.onCommand("hastebin") {
                val data = it.getOption("data")!!.asString
//                val httpService = HttpService()
//                val post = httpService.buildPost("https://paste.learnspigot.com/documents", data)
//                val result = httpService.sendStringRequest(post)
                val result = HastebinService().createDocument(data)
                it.replyEmbed({
                    title = "Created Hastebin"
                    description = "You can view the hastebin here: https://paste.learnspigot.com/${result}"
                    color = EMBED_COLOR
                }).queue()
            }
        }.queue()
    }
}