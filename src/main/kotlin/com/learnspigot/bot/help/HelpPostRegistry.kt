package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.PostRegistry

class HelpPostRegistry : PostRegistry() {

    init {
        Server.helpChannel.threadChannels.forEach {
            posts[it.name] = it.id
        }
    }

}