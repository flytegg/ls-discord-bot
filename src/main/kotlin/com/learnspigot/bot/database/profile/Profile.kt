package com.learnspigot.bot.database.profile

import com.google.gson.annotations.SerializedName
import com.learnspigot.bot.Bot
import com.learnspigot.bot.database.Mongo
import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.InvisibleEmbed
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import org.litote.kmongo.save
import java.time.Instant
import java.util.*

data class Profile(
    @SerializedName("_id") val id: String,
    val tag: String?,
    var udemyProfileUrl: String?,
    val reputation: NavigableMap<Int, Reputation>,
    val notifyOnRep: Boolean,
    var intellijKeyGiven: Boolean,
    var highestCount: Int,
    var totalCounts: Int,
    var countingFuckUps: Int
) {
    fun getUser() = Bot.jda.getUserById(id)!!

    fun save() = Mongo.userCollection.save(this)

    fun addReputation(fromUserId: String, fromPostId: String, amount: Int) {
        reputation[if (reputation.isEmpty()) 0 else reputation.lastKey() + 1] = Reputation(Instant.now().epochSecond, fromUserId, fromPostId)
        save()

        Bot.jda.getUserById(id)!!.openPrivateChannel().complete().also { privateChannel ->
            privateChannel.sendMessageEmbeds(
                InvisibleEmbed {
                    title = "You earned ${if (amount == 1) "" else "$amount "}reputation"
                    description = "You gained reputation from <@$fromUserId> in <#$fromPostId>."

                    author {
                        name = "You have ${reputation.size} reputation in total"
                    }
                }
            ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
        }
    }
}