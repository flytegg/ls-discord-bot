package com.learnspigot.bot.database.counting

import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.database.Mongo.countingCollection
import com.learnspigot.bot.database.Mongo.userCollection
import com.learnspigot.bot.database.profile.fuckedUpCounting
import com.learnspigot.bot.database.profile.getProfile
import com.learnspigot.bot.database.profile.incrementCount
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.descending
import org.litote.kmongo.replaceOneById

object CountingManager {
    private var documentId: ObjectId
    var topServerCount: Int = 0
    var serverTotalCounts: Int = 0
    var currentCount = 0


    fun getTop5() = userCollection.find()
        .sort(descending(Profile::totalCounts))
        .limit(5)
        .toList()

    init {
        val document = countingCollection.find().firstOrNull() ?: Counting()
        documentId = document.id
        topServerCount = document.highestCount
        currentCount = document.currentCount
        serverTotalCounts = document.serverTotalCounts
    }

    fun incrementCount(user: User) {
        currentCount++
        serverTotalCounts++
        if (currentCount > topServerCount) topServerCount = currentCount

        user.getProfile()?.incrementCount(currentCount)

        val document = Counting(
            highestCount = topServerCount,
            currentCount = currentCount,
            serverTotalCounts = serverTotalCounts
        )

        countingCollection.replaceOneById(documentId, document)
    }

    fun fuckedUp(user: User) {
        currentCount = 0
        user.getProfile()?.fuckedUpCounting()
    }

}