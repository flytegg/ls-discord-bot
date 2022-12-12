package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.http.FinanceService
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

class WixStockCommand(guild: Guild, bot: JDA) {

    init {
        guild.upsertCommand("wixstock", "Realtime stock price of WIX") {
            restrict(guild = true)
            bot.onCommand("wixstock") {
                val financeService = FinanceService()
                it.replyEmbed({
                    title = "Wix Stock"
                    color = EMBED_COLOR
                    field("Current Value (USD)", financeService.getPriceOfStock("WIX"), true)
                }).queue()
            }
        }
    }
}