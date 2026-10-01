package com.learnspigot.bot.profile

import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import org.bson.Document
import java.time.Instant
import java.util.*

data class Profile(
    val id: String,
    val tag: String?,
    var udemyProfileUrl: String?,
    val reputation: NavigableMap<Int, Reputation>,
    val notifyOnRep: Boolean,
    var intellijKeyLastGiven: Long?, // Epoch seconds of the last key given, null if never
    var highestCount: Int,
    var totalCounts: Int,
    var countingFuckUps: Int,
    var countingBans: Int = 0,
    var countingBanExpiry: Long? = null // Epoch seconds, null when not on a timed ban
) {

    fun addReputation(user: User, fromUserId: String, fromPostId: String, amount: Int) {
        for (i in 0 until amount)
            reputation[if (reputation.isEmpty()) 0 else reputation.lastKey() + 1] =
                Reputation(Instant.now().epochSecond, fromUserId, fromPostId)

        save()

        user.openPrivateChannel().queue {
            it.sendMessageEmbeds(
                embed()
                    .setAuthor("You have ${reputation.size} reputation in total")
                    .setTitle("You earned ${if (amount == 1) "" else "$amount "}reputation")
                    .setDescription("You gained reputation from <@$fromUserId> in <#$fromPostId>.")
                    .build()
            ).queue(null) {
                println("[DM DISABLED] Unable to DM '${user.name}' about their reputation")
            }
        }
    }

    fun removeReputation(startId: Int, endId: Int) {
        for (i in startId..endId) {
            reputation.remove(i)
        }
        save()
    }

    fun save() {
        val document = Document()
        document["_id"] = id
        document["tag"] = tag
        document["udemyProfileUrl"] = udemyProfileUrl
        val reputationDocument = Document()
        reputation.forEach { (id, rep) ->
            reputationDocument[id.toString()] = rep.document()
        }
        document["reputation"] = reputationDocument
        document["notifyOnRep"] = notifyOnRep
        document["intellijKeyLastGiven"] = intellijKeyLastGiven
        document["highestCount"] = highestCount
        document["totalCounts"] = totalCounts
        document["countingFuckUps"] = countingFuckUps
        document["countingBans"] = countingBans
        document["countingBanExpiry"] = countingBanExpiry
        Mongo.userCollection.replaceOne(Filters.eq("_id", id), document, ReplaceOptions().upsert(true))
    }

    fun incrementCount(currentCount: Int) {
        totalCounts++
        if (currentCount > highestCount) highestCount = currentCount
        saveCounting()
    }

    fun fuckedUpCounting() {
        countingFuckUps++
        saveCounting()
    }

    fun countingBanned(expiry: Long?) {
        countingBans++
        countingBanExpiry = expiry
        saveCounting()
    }

    fun countingBanExpired() {
        countingBanExpiry = null
        saveCounting()
    }

    private fun saveCounting() {
        val doc = Mongo.userCollection.find(Filters.eq("_id", id)).first()!!
        doc["highestCount"] = highestCount
        doc["totalCounts"] = totalCounts
        doc["countingFuckUps"] = countingFuckUps
        doc["countingBans"] = countingBans
        doc["countingBanExpiry"] = countingBanExpiry
        Mongo.userCollection.replaceOne(Filters.eq("_id", id), doc, ReplaceOptions().upsert(true))
    }

}
