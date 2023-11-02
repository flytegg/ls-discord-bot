package com.learnspigot.bot.help.search

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.PostRegistry

class HelpPostRegistry : PostRegistry() {

    init {
        // Get open help posts
        Server.helpChannel.threadChannels.forEach { posts[it.name] = it.id }
        // Get closed help posts
        Server.helpChannel.retrieveArchivedPublicThreadChannels().forEach { posts[it.name] = it.id }
    }

}