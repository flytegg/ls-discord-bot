package com.learnspigot.bot.database.profile

import com.learnspigot.bot.database.Mongo.userCollection
import org.litote.kmongo.findOneById
import java.util.*

object ProfileManager {
    val profileCache: MutableMap<String, Profile> = TreeMap(String.CASE_INSENSITIVE_ORDER)

    fun getProfile(id: String): Profile? = profileCache[id] ?: userCollection.findOneById(id).also { user ->
        if (user != null) profileCache[user.id] = user
    }

    fun getProfileByURL(url: String): Profile? = profileCache.values.find { it.udemyProfileUrl.equals(url, true) }
}