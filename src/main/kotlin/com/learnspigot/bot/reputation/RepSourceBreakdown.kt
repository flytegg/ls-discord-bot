package com.learnspigot.bot.reputation

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel

/**
 * Logs where everyone's reputation came from, to explain the gap between the all-time leaderboard
 * and the help post stats. Read only, nothing is changed.
 */
object RepSourceBreakdown {

    enum class Source(val label: String) {
        HELP("help posts"),
        KNOWLEDGEBASE("knowledgebase votes"),
        PROJECTS("project votes"),
        NO_POST("no post recorded (pre-3.0)"),
        OTHER_CHANNEL("other channels (/addrep)"),
        UNKNOWN("unknown channel (likely deleted)")
    }

    fun log() {
        val helpPosts = Registry.HELP_STATS.posts.keys
        val knowledgebasePosts = threadIds(Server.CHANNEL_KNOWLEDGEBASE)
        val projectPosts = threadIds(Server.CHANNEL_PROJECTS)

        fun sourceOf(rep: Reputation): Source {
            val postId = rep.fromPostId ?: return Source.NO_POST
            return when {
                postId in helpPosts -> Source.HELP
                postId in knowledgebasePosts -> Source.KNOWLEDGEBASE
                postId in projectPosts -> Source.PROJECTS
                Server.GUILD.getGuildChannelById(postId) != null -> Source.OTHER_CHANNEL
                else -> Source.UNKNOWN
            }
        }

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
