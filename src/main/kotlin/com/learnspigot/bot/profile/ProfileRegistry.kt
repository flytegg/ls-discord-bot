package com.learnspigot.bot.profile

import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.Mongo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import java.util.*

class ProfileRegistry {

    val profileCache: MutableMap<String, Profile> = TreeMap(String.CASE_INSENSITIVE_ORDER)

    val contributorSelectorCache: MutableMap<String, List<String>> = HashMap()
    val messagesToRemove: MutableMap<String, Message> = HashMap()

    init {
        Mongo.userCollection.find().forEach { document ->
            val reputation: NavigableMap<Int, Reputation> = TreeMap()
            document.get("reputation", Document::class.java).forEach { id, rep ->
                val repDocument = rep as Document
                reputation[id.toInt()] = Reputation(
                    repDocument.getLong("timestamp"),
                    repDocument.getString("fromMemberId"),
                    repDocument.getString("fromPostId"))
            }

            Profile(
                document.getString("_id"),
                document.getString("tag"),
                document.getString("udemyProfileUrl"),
                reputation,
                document.getBoolean("notifyOnRep") ?: true,
                document.getBoolean("intellijKeyGiven") ?: false).let {
                    profileCache[it.id] = it
            }
        }
    }

    fun findById(id: String): Profile? {
        return profileCache[id]
    }

    fun findByUser(user: User): Profile {
        return findById(user.id) ?: run {
            Profile(
                user.id,
                user.asTag,
                null,
                TreeMap(),
                true,
                false).apply {
                    profileCache[user.id] = this
                    save()
                }
        }
    }

}