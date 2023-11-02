package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Server
import com.learnspigot.bot.lecture.WordMatcher
import com.learnspigot.bot.util.PostRegistry
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

class KnowledgebaseRegistry : PostRegistry() {

    init {
        Server.knowledgebaseChannel.threadChannels.forEach {
            posts[it.name] = it.id
        }
    }

}