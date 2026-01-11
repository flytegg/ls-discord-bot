package com.learnspigot.bot.workshop

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.closeAndLock
import com.learnspigot.bot.util.embed
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class WorkShopPostRegistry {

    val posts: HashMap<String, String> = hashMapOf() // post-id / owner-id

    fun getInfo() {
        CompletableFuture.runAsync({
            for (channel in Server.workshopChannel.threadChannels) {
                if (channel.owner == null) {
                    channel.sendMessageEmbeds(
                        embed().setTitle("Workshop close.").setDescription("Closing workshop because owner isn't in the server.").build()
                    ).queue()
                    channel.closeAndLock()
                    continue
                }
                posts[channel.id] = channel.owner!!.id
            }

        }, Executors.newCachedThreadPool())
    }
}