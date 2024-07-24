package com.learnspigot.bot.counting

import com.learnspigot.bot.Bot
import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.database.Mongo
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.User
import org.bson.Document

class CountingRegistry(val bot: Bot) {
    private inline val profileRegistry get() = bot.profileRegistry()
    private val mongoCollection = Mongo.countingCollection

    var topServerCount: Int = 0
    var serverTotalCounts: Int = 0
    var currentCount = 0

    fun getTop5(): List<Profile> = profileRegistry.profileCache.values
        .filter { it.totalCounts > 0 }
        .sortedByDescending { it.totalCounts }
        .take(5)

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

    fun incrementCount(user: User) {
        currentCount++
        serverTotalCounts++
        if (currentCount > topServerCount) topServerCount = currentCount
        profileRegistry.findByUser(user).incrementCount(currentCount)
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