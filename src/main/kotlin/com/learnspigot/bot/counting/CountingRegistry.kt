package com.learnspigot.bot.counting

import com.learnspigot.bot.Bot
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import java.util.*

class CountingRegistry(val bot: Bot) {
    private inline val profileRegistry get() = bot.profileRegistry()
    private val mongoCollection = Mongo.countingCollection

    var topServerCount: Int = 0
    var serverTotalCounts: Int = 0
    var currentCount = 0

    // Queue of User Ids
    val leaderboard: PriorityQueue<String> = PriorityQueue { a, b ->
        profileRegistry.findById(b)!!.totalCounts.compareTo(profileRegistry.findById(a)!!.totalCounts)
    }

    init {
        val document = mongoCollection.find().first()
        if (document == null) {
            val newDoc = Document()
            newDoc["highestCount"] = 0
            newDoc["currentCount"] = 0
            newDoc["serverTotalCounts"] = 0
            mongoCollection.insertOne(newDoc)
        } else {
            topServerCount = document.getInteger("highestCount", 0)
            currentCount = document.getInteger("currentCount", 0)
            serverTotalCounts = document.getInteger("serverTotalCounts", 0)
        }
    }

    fun initLeaderboard(profileRegistry: ProfileRegistry) {
        profileRegistry.profileCache.values.forEach { if (!leaderboard.contains(it.id)) leaderboard.add(it.id) }
    }

    private fun <T> PriorityQueue<T>.recalculate(ele: T) = remove(ele).also { add(ele) }

    fun incrementCount(user: User) {
        currentCount++
        serverTotalCounts++
        if (currentCount > topServerCount) topServerCount = currentCount
        profileRegistry.findByUser(user).incrementCount(currentCount)
        leaderboard.recalculate(user.id)
        val newDoc = mongoCollection.find().first()!!
        newDoc["highestCount"] = topServerCount
        newDoc["currentCount"] = currentCount
        newDoc["serverTotalCounts"] = serverTotalCounts
        mongoCollection.replaceOne(Filters.eq("_id", newDoc.getObjectId("_id")), newDoc)
    }

    fun fuckedUp(user: User) {
        currentCount = 0
        profileRegistry.findByUser(user).fuckedUpCounting()
    }

}