package com.learnspigot.bot.util

import com.learnspigot.bot.Server
import com.learnspigot.bot.videos.WordMatcher
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

open class PostRegistry {

    val posts = mutableMapOf<String, String>() // name, id
    private val matcher: WordMatcher = WordMatcher()

    fun findTop4Posts(query: String): MutableList<ThreadChannel> {
        return matcher.getTopMatches(query, posts.keys.toList(), 4).mapNotNull { name ->
            Server.GUILD.getThreadChannelById(posts[name]!!) ?: let { _ ->
                println("JDA Cache miss on $name. Refreshing cache...")
                Server.GUILD.retrieveActiveThreads().complete().find { it.id == posts[name]!! }
            }
        }.toMutableList()
    }

}