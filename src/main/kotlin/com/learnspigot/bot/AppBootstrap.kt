package com.learnspigot.bot

import com.learnspigot.bot.util.DiscordCommandsDeleter
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println(args.joinToString(", "))
    if(args.isNotEmpty()  && args[0] == "deleteCommands") {
        DiscordCommandsDeleter()
        exitProcess(0)
    }
    LearnSpigotBot()
}