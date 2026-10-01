package com.learnspigot.bot.reputation

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.util.concurrent.ConcurrentHashMap

/**
 * Works out where a piece of reputation came from, so help post stats only count rep earned helping
 * and leave out knowledgebase votes, project votes and /addrep in other channels.
 */
object RepSources {

    enum class Source(val label: String, val countsAsHelp: Boolean) {
        HELP("help posts", true),
        NO_POST("no post recorded (pre-3.0)", true), // Before 3.0 rep could only be earned in help posts
        UNKNOWN("unknown channel (likely deleted help post)", true),
        KNOWLEDGEBASE("knowledgebase votes", false),
        PROJECTS("project votes", false),
        OTHER_CHANNEL("other channels (/addrep)", false)
    }

    private val knowledgebasePosts: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val projectPosts: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * Loads every knowledgebase and project post, so rep from them is recognised even once they're archived.
     */
    fun load() {
        knowledgebasePosts.addAll(threadIds(Server.CHANNEL_KNOWLEDGEBASE))
        projectPosts.addAll(threadIds(Server.CHANNEL_PROJECTS))
    }

    /**
     * Remembers a post that rep was voted for, in case it's created after [load] and later falls out of the cache.
     */
    fun voteRepGiven(thread: ThreadChannel) {
        if (thread.parentChannel.isChannel(Server.CHANNEL_KNOWLEDGEBASE)) knowledgebasePosts.add(thread.id)
        else if (thread.parentChannel.isChannel(Server.CHANNEL_PROJECTS)) projectPosts.add(thread.id)
    }

    fun sourceOf(rep: Reputation): Source {
        val postId = rep.fromPostId ?: return Source.NO_POST
        if (postId in Registry.HELP_STATS.posts) return Source.HELP
        if (postId in knowledgebasePosts) return Source.KNOWLEDGEBASE
        if (postId in projectPosts) return Source.PROJECTS

        val channel = Server.GUILD.getGuildChannelById(postId) ?: return Source.UNKNOWN
        val parent = (channel as? ThreadChannel)?.parentChannel
        return when {
            parent?.isChannel(Server.CHANNEL_HELP) == true -> Source.HELP
            parent?.isChannel(Server.CHANNEL_KNOWLEDGEBASE) == true -> Source.KNOWLEDGEBASE
            parent?.isChannel(Server.CHANNEL_PROJECTS) == true -> Source.PROJECTS
            else -> Source.OTHER_CHANNEL
        }
    }

    /**
     * Logs where everyone's reputation came from, to explain the gap between the all-time leaderboard
     * and the help post stats.
     */
    fun log() {
        val profiles = Registry.PROFILES.profileCache.values.toList()
        val bySource = profiles.associate { profile -> profile.id to profile.reputation.values.groupingBy(::sourceOf).eachCount() }

        fun format(counts: Map<Source, Int>) = Source.entries.joinToString(" | ") { "${it.label}: ${counts[it] ?: 0}" }

        val totals = Source.entries.associateWith { source -> bySource.values.sumOf { it[source] ?: 0 } }
        println("[Rep Sources] All rep (${totals.values.sum()}): ${format(totals)}")

        // Same top 10 as the all-time leaderboard
        profiles.sortedByDescending { it.reputation.size }.take(10).forEach { profile ->
            println("[Rep Sources] ${profile.tag ?: profile.id} (${profile.reputation.size}): ${format(bySource[profile.id]!!)}")
        }
    }

    private fun threadIds(forum: ForumChannel): Set<String> {
        val ids = forum.threadChannels.map { it.id }.toMutableSet()
        forum.retrieveArchivedPublicThreadChannels().forEach { ids.add(it.id) }
        return ids
    }

}
