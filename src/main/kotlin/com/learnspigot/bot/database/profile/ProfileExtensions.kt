package com.learnspigot.bot.database.profile

import com.learnspigot.bot.database.Mongo.userCollection
import com.learnspigot.bot.reputation.Reputation
import org.litote.kmongo.save
import java.time.Instant

fun Profile.addReputation(fromUserId: String, fromPostId: String, amount: Int) {
    reputation[if (reputation.isEmpty()) 0 else reputation.lastKey() + 1] = Reputation(Instant.now().epochSecond, fromUserId, fromPostId)
}

fun Profile.save() {
    userCollection.save(this)
}