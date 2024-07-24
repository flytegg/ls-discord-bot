package com.learnspigot.bot

import io.github.cdimascio.dotenv.Dotenv

object Environment {
    private val dotenv = Dotenv.configure()
        .systemProperties()
        .load()

    val BOT_TOKEN get() = get("BOT_TOKEN")
    val GUILD_ID get() = get("GUILD_ID")

    fun get(variable: String): String {
        return dotenv.get(variable)
    }
}