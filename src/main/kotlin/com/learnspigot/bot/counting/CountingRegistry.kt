package com.learnspigot.bot.counting

import com.learnspigot.bot.Bot
import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import java.time.LocalDateTime

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

    fun fuckedUp(guild: Guild, user: User) {
        val staff = arrayOf(914974165432414278, 808093118825234432, 804147192758534164)
        val isStaff = { member: Member -> member.roles.any { it.id.toLong() in staff } }

        if (guild.getMember(user)?.let { isStaff(it) } == true) {
            currentCount = 0
            profileRegistry.findByUser(user).fuckedUpCounting("N/A")
            return
        }

        if (currentCount < 75) {
            currentCount = 0
            profileRegistry.findByUser(user).fuckedUpCounting("N/A")
            return
        }

        val hoursMuted = (((currentCount - 1) / 75) + 1) * 120L
        val muteTime = LocalDateTime.now().plusHours(hoursMuted).toString()

        profileRegistry.findByUser(user).fuckedUpCounting(muteTime)

        currentCount = 0
    }

}