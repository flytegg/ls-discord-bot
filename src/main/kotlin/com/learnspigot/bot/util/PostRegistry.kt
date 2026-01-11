package com.learnspigot.bot.util

import com.learnspigot.bot.Server
import com.learnspigot.bot.videos.WordMatcher
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

open class PostRegistry {

    val posts = mutableMapOf<String, String>() // name, id
    private val matcher: WordMatcher = WordMatcher()

    fun findTop4Posts(query: String): MutableList<ThreadChannel> {
        return matcher.getTopMatches(query, posts.keys.toList(), 4).mapNotNull {
            Server.GUILD.getThreadChannelById(posts[it]!!)
        }.toMutableList()
    }

}