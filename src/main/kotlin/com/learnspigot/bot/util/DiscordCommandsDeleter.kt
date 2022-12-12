package com.learnspigot.bot.util

import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.requests.GatewayIntent

class DiscordCommandsDeleter {

    val bot = light(System.getenv("DISCORD_TOKEN")!!) {
        enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
    }

    init {
        println("Waiting to log in")
        bot.awaitReady()
        println("Looping through commands")
        bot.retrieveCommands().complete().forEach {
            println("Deleting ${it.name} command")
            it.delete().queue()
        }
        println("Done - exiting.")
    }
}