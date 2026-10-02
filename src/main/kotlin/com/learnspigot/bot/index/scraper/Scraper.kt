package com.learnspigot.bot.index.scraper

import com.learnspigot.bot.Bot
import com.learnspigot.bot.index.IndexEntry
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

interface Scraper {
    val canLog: Boolean get() = Bot.fromEnv("GUILD_ID") != "397526357191557121"

    companion object {
        val mainExecutor: ExecutorService =
            Executors.newThreadPerTaskExecutor {
                Thread(it, "SCRAPE-Worker-" + (Random().nextInt(999) + 1))
            }
        val poolExecutor: ExecutorService =
            Executors.newFixedThreadPool(10) {
                Thread(it, "SCRAPE-Worker-" + (Random().nextInt(999) + 1))
            }
    }

    fun scrape(afterFinish: Consumer<List<IndexEntry>>)
}
