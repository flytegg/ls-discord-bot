package com.learnspigot.bot.help.stats

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.Updates
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import org.bson.Document
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Keeps a record of every help post in Mongo so stats can be built without asking Discord.
 *
 * The first run backfills every past post. After that, posts are updated live by [HelpStatsListener],
 * and each startup only re-checks open posts and posts archived since the bot was last running.
 */
class HelpStatsRegistry {

    private val collection get() = Mongo.helpPostsCollection
    private val metaCollection get() = Mongo.helpPostsMetaCollection

    val posts: MutableMap<String, HelpPost> = ConcurrentHashMap()

    @Volatile
    var syncing = true
        private set

    init {
        collection.find().forEach { HelpPost.fromDocument(it).let { post -> posts[post.id] = post } }

        CompletableFuture.runAsync({
            try {
                sync()
            } catch (e: Exception) {
                println("[Help Stats] Sync failed, will retry next startup")
                e.printStackTrace()
            }
            syncing = false
        }, Executors.newSingleThreadExecutor())

        // Remember when we were last running, so the next startup knows how far back to check
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            if (!syncing) setLastSeen(Instant.now().epochSecond)
        }, 1L, 1L, TimeUnit.MINUTES)
    }

    private fun sync() {
        val lastSeen = metaCollection.find().first()?.getLong("lastSeen")
        val startedAt = Instant.now().epochSecond
        println(if (lastSeen == null) "[Help Stats] Backfilling every help post, this may take a few minutes..." else "[Help Stats] Checking for missed help post updates...")

        var checked = 0
        Server.CHANNEL_HELP.threadChannels.forEach { sync(it); checked++ }
        // Archived posts come newest-archived first, so stop once we reach ones archived before we went offline
        Server.CHANNEL_HELP.retrieveArchivedPublicThreadChannels().forEachRemaining { thread ->
            if (lastSeen != null && thread.timeArchiveInfoLastModified.toEpochSecond() < lastSeen) return@forEachRemaining false
            sync(thread)
            if (++checked % 100 == 0) println("[Help Stats] Checked $checked posts...")
            true
        }

        setLastSeen(startedAt)
        println("[Help Stats] Sync complete, checked $checked posts (${posts.size} stored)")
    }

    /**
     * Brings a stored post up to date with Discord, creating it (and finding its first response) if needed.
     */
    fun sync(thread: ThreadChannel) {
        val post = posts.getOrPut(thread.id) {
            HelpPost(thread.id, thread.ownerId, thread.timeCreated.toEpochSecond())
        }
        // Discord's count excludes the starter message
        post.messageCount = maxOf(post.messageCount, thread.messageCount + 1)
        post.closedAt = if (thread.isArchived && thread.isLocked) thread.timeArchiveInfoLastModified.toEpochSecond() else null
        if (post.firstResponseAt == null) post.firstResponseAt = findFirstResponse(thread)
        save(post)
    }

    private fun findFirstResponse(thread: ThreadChannel): Long? = try {
        thread.getHistoryFromBeginning(100).complete().retrievedHistory
            .filter { it.author.id != thread.ownerId && !it.author.isBot && !it.author.isSystem }
            .minOfOrNull { it.timeCreated.toEpochSecond() }
    } catch (e: Exception) {
        null
    }

    fun postCreated(thread: ThreadChannel) {
        val post = HelpPost(thread.id, thread.ownerId, thread.timeCreated.toEpochSecond(), messageCount = 1)
        posts[post.id] = post
        save(post)
    }

    fun messageSent(thread: ThreadChannel, authorId: String, isBot: Boolean, sentAt: Long) {
        val post = posts[thread.id] ?: return sync(thread)
        post.messageCount++
        val updates = mutableListOf(Updates.inc("messageCount", 1))
        if (post.firstResponseAt == null && authorId != post.ownerId && !isBot) {
            post.firstResponseAt = sentAt
            updates.add(Updates.set("firstResponseAt", sentAt))
        }
        collection.updateOne(Filters.eq("_id", post.id), Updates.combine(updates))
    }

    fun postDeleted(id: String) {
        if (posts.remove(id) == null) return
        collection.deleteOne(Filters.eq("_id", id))
    }

    private fun save(post: HelpPost) {
        collection.replaceOne(Filters.eq("_id", post.id), post.document(), ReplaceOptions().upsert(true))
    }

    private fun setLastSeen(time: Long) {
        metaCollection.replaceOne(Filters.eq("_id", "meta"), Document("_id", "meta").append("lastSeen", time), ReplaceOptions().upsert(true))
    }

    fun stats(): HelpStats {
        val now = Instant.now().epochSecond
        val week = now - Duration.ofDays(7).seconds
        val month = now - Duration.ofDays(30).seconds
        val all = posts.values.toList()
        val closed = all.filter { it.closedAt != null }
        val postsThisWeek = all.filter { it.createdAt >= week }

        // Contributors are whoever got rep from a post, counted once each
        val contributorsByPost = mutableMapOf<String, MutableSet<String>>()
        val repThisWeek = mutableMapOf<String, Int>()
        Registry.PROFILES.profileCache.values.toList().forEach { profile ->
            profile.reputation.values.forEach { rep ->
                rep.fromPostId?.let { contributorsByPost.getOrPut(it) { mutableSetOf() }.add(profile.id) }
                if (rep.timestamp >= week) repThisWeek.merge(profile.id, 1, Int::plus)
            }
        }

        return HelpStats(
            open = Server.CHANNEL_HELP.threadChannels.count { !it.isArchived },
            avgResponseSeconds = all.mapNotNull { post -> post.firstResponseAt?.let { it - post.createdAt } }.filter { it >= 0 }.average(),
            avgResolutionSeconds = closed.map { it.closedAt!! - it.createdAt }.average(),
            closedWeek = closed.count { it.closedAt!! >= week },
            closedMonth = closed.count { it.closedAt!! >= month },
            closedTotal = closed.size,
            avgMessagesWeek = postsThisWeek.map { it.messageCount }.average(),
            avgMessagesTotal = all.map { it.messageCount }.average(),
            avgContributors = closed.map { contributorsByPost[it.id]?.size ?: 0 }.average(),
            topContributorWeek = repThisWeek.maxByOrNull { it.value }?.key,
            mostMessages = all.maxByOrNull { it.messageCount },
            mostPosts = all.groupingBy { it.ownerId }.eachCount().maxByOrNull { it.value }?.toPair()
        )
    }

    data class HelpStats(
        val open: Int,
        val avgResponseSeconds: Double,
        val avgResolutionSeconds: Double,
        val closedWeek: Int,
        val closedMonth: Int,
        val closedTotal: Int,
        val avgMessagesWeek: Double,
        val avgMessagesTotal: Double,
        val avgContributors: Double,
        val topContributorWeek: String?,
        val mostMessages: HelpPost?,
        val mostPosts: Pair<String, Int>?
    )
}
