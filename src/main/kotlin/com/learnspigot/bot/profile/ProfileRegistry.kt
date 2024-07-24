package com.learnspigot.bot.profile

import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.database.Mongo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import org.bson.Document
import java.util.*

class ProfileRegistry {

    val profileCache: MutableMap<String, Profile> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val urlProfiles: MutableMap<String, Profile> = TreeMap()

    val contributorSelectorCache: MutableMap<String, List<String>> = HashMap()
    val messagesToRemove: MutableMap<String, Message> = HashMap()

    init {
        Mongo.userCollection.find().forEach { document ->
            val reputation: NavigableMap<Int, Reputation> = TreeMap()
            document.get("reputation", Document::class.java).forEach { id, rep ->
                val repDocument = rep as Document
                reputation[id.toInt()] = Reputation(
                    convertToLongTimestamp(repDocument["timestamp"]!!),
                    repDocument.getString("fromMemberId"),
                    repDocument.getString("fromPostId"))
            }

            Profile(
                document.getString("_id"),
                document.getString("tag"),
                document.getString("udemyProfileUrl"),
                reputation,
                document.getBoolean("notifyOnRep", true),
                document.getBoolean("intellijKeyGiven", false),
                document.getInteger("highestCount", 0),
                document.getInteger("totalCounts", 0),
                document.getInteger("countingFuckUps", 0)
            ).let {
                profileCache[it.id] = it
                if (it.udemyProfileUrl != null)
                    urlProfiles[it.udemyProfileUrl!!] = it
            }
        }
    }

    // I don't even care enough to sort this bug so have this function instead
    // Basically at some point they've been saving as Ints and some points Longs. So now we must read both. .-.
    private fun convertToLongTimestamp(timestamp: Any): Long {
        return when (timestamp) {
            is Int -> timestamp.toLong()
            is Long -> timestamp.toLong()
            is String -> timestamp.toLongOrNull() ?: throw IllegalArgumentException("Invalid timestamp format")
            else -> throw IllegalArgumentException("Unsupported timestamp format")
        }
    }

    fun findById(id: String): Profile? {
        return profileCache[id]
    }

    fun findByUser(user: User): Profile {
        return findById(user.id) ?: run {
            Profile(
                user.id,
                user.name,
                null,
                TreeMap(),
                true,
                false,
                0,
                0,
                0,
            ).apply {
                    profileCache[user.id] = this
                    save()
                }
        }
    }

    fun findByURL(udemyURL: String): Profile? {
        return urlProfiles[udemyURL]
    }
}