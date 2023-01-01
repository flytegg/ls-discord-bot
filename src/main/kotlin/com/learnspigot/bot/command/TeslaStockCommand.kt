package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.http.FinanceService
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

class TeslaStockCommand(guild: Guild, bot: JDA) {

    init {
        guild.upsertCommand("teslastock", "Realtime stock price of Tesla") {
            restrict(guild = true)
            bot.onCommand("teslastock") {
                it.deferReply().queue()
                val financeService = FinanceService()
                it.replyEmbed({
                    title = "Tesla Stock"
                    color = EMBED_COLOR
                    field("Current Value (USD)", financeService.getPriceOfStock("TSLA"), true)
                }).queue()
            }
        }.queue()
    }
}