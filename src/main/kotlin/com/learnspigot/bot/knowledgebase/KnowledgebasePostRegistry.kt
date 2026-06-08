package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.PostRegistry
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class KnowledgebasePostRegistry : PostRegistry() {

    init {
        CompletableFuture.runAsync({
            val unarchived = Server.CHANNEL_KNOWLEDGEBASE.threadChannels.onEach { posts[it.name] = it.id }
            val archived = Server.CHANNEL_KNOWLEDGEBASE.retrieveArchivedPublicThreadChannels().complete().onEach { posts[it.name] = it.id }
            println("[KNOWLEDGEBASE] Loaded threads (${archived.size + unarchived.size}) - archived: ${archived.size} | unarchived: ${unarchived.size}")
        }, Executors.newCachedThreadPool())
    }

}