package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.PostRegistry
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class KnowledgebasePostRegistry : PostRegistry() {

    init {
        CompletableFuture.runAsync({
            Server.knowledgebaseChannel.threadChannels.forEach {
                posts[it.name] = it.id
            }
        }, Executors.newCachedThreadPool())
    }

}