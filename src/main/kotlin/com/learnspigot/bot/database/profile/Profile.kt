package com.learnspigot.bot.database.profile

import com.learnspigot.bot.Bot
import com.learnspigot.bot.reputation.Reputation
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
    fun getUser() = Bot.jda.getUserById(id)!!
}