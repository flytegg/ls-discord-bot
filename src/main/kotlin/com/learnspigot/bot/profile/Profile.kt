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
    var intellijKeyGiven: Boolean,
    var highestCount: Int,
    var totalCounts: Int,
    var countingFuckUps: Int
) {

    fun addReputation(user: User, fromUserId: String, fromPostId: String, amount: Int) {
        for (i in 0 until amount)
            reputation[if (reputation.isEmpty()) 0 else reputation.lastKey() + 1] =
                Reputation(Instant.now().epochSecond, fromUserId, fromPostId)

        save()

        user.openPrivateChannel().complete().let {
            it.sendMessageEmbeds(
                embed()
                    .setAuthor("You have ${reputation.size} reputation in total")
                    .setTitle("You earned ${if (amount == 1) "" else "$amount "}reputation")
                    .setDescription("You gained reputation from <@$fromUserId> in <#$fromPostId>.")
                    .build()
            ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
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
        document["intellijKeyGiven"] = intellijKeyGiven
        document["highestCount"] = highestCount
        document["totalCounts"] = totalCounts
        document["countingFuckUps"] = countingFuckUps
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

    private fun saveCounting() {
        val doc = Mongo.userCollection.find(Filters.eq("_id", id)).first()!!
        doc["highestCount"] = highestCount
        doc["totalCounts"] = totalCounts
        doc["countingFuckUps"] = countingFuckUps
        Mongo.userCollection.replaceOne(Filters.eq("_id", id), doc, ReplaceOptions().upsert(true))
    }

}
