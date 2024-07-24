package com.learnspigot.bot.database.counting

import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.database.Mongo.countingCollection
import com.learnspigot.bot.database.Mongo.userCollection
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import org.litote.kmongo.descending

object CountingManager {
    var topServerCount: Int = 0
    var serverTotalCounts: Int = 0
    var currentCount = 0


    fun getTop5() = userCollection.find()
        .sort(descending(Profile::totalCounts))
        .limit(5)
        .toList()

    init {
        val document = countingCollection.find().firstOrNull() ?: Counting()
        topServerCount = document.highestCount
        currentCount = document.currentCount
        serverTotalCounts = document.serverTotalCounts
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