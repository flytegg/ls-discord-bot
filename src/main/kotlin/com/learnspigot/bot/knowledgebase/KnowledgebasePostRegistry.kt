package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.PostRegistry

class KnowledgebasePostRegistry : PostRegistry() {

    init {
        Server.knowledgebaseChannel.threadChannels.forEach {
            posts[it.name] = it.id
        }
    }

}