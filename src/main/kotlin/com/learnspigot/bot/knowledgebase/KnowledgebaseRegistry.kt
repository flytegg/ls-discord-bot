package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Server
import com.learnspigot.bot.lecture.WordMatcher
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

class KnowledgebaseRegistry {

    val posts = mutableMapOf<String, String>() // name, id
    private val matcher: WordMatcher = WordMatcher()

    init {
        Server.knowledgebaseChannel.threadChannels.forEach {
            posts[it.name] = it.id
        }
    }

    fun findTop4Posts(query: String): MutableList<ThreadChannel> {
        return matcher.getTopMatches(query, posts.keys.toList(), 4).mapNotNull {
            Server.guild.getThreadChannelById(posts[it]!!)
        }.toMutableList()
    }

}