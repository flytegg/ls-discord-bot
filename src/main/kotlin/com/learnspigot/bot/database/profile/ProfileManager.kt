package com.learnspigot.bot.database.profile

import com.learnspigot.bot.database.Mongo.userCollection
import net.dv8tion.jda.api.entities.Message
import org.litote.kmongo.findOneById
import java.util.*

object ProfileManager {
    val profileCache: MutableMap<String, Profile> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val urlProfiles: MutableMap<String, Profile> = TreeMap()

    val contributorSelectorCache: MutableMap<String, List<String>> = HashMap()
    val messagesToRemove: MutableMap<String, Message> = HashMap()

    fun getProfile(id: String): Profile? = profileCache[id] ?: userCollection.findOneById(id).also { user ->
        if (user != null) profileCache[user.id] = user
    }
}