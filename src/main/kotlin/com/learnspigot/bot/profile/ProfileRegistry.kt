package com.learnspigot.bot.profile

import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
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
                document.getLong("intellijKeyLastGiven"),
                document.getInteger("highestCount", 0),
                document.getInteger("totalCounts", 0),
                document.getInteger("countingFuckUps", 0),
                document.getInteger("countingBans", 0),
                document.getLong("countingBanExpiry")
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
                null,
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

    /**
     * Moves everything from the old profile onto the new one, then deletes the old profile.
     * If the new account already has data, the two are merged.
     */
    fun transfer(old: Profile, new: Profile) {
        val combinedReputation = (old.reputation.values + new.reputation.values).sortedBy { it.timestamp }
        new.reputation.clear()
        combinedReputation.forEachIndexed { id, rep -> new.reputation[id] = rep }

        if (new.udemyProfileUrl == null) new.udemyProfileUrl = old.udemyProfileUrl
        new.highestCount = maxOf(new.highestCount, old.highestCount)
        new.totalCounts += old.totalCounts
        new.countingFuckUps += old.countingFuckUps
        new.countingBans += old.countingBans
        new.countingBanExpiry = listOfNotNull(new.countingBanExpiry, old.countingBanExpiry).maxOrNull()
        // Keep the /getkey cooldown so a transfer can't be used to claim an extra key
        new.intellijKeyLastGiven = listOfNotNull(new.intellijKeyLastGiven, old.intellijKeyLastGiven).maxOrNull()
        new.save()

        profileCache.remove(old.id)
        old.udemyProfileUrl?.let { if (urlProfiles[it] == old) urlProfiles.remove(it) }
        new.udemyProfileUrl?.let { urlProfiles[it] = new }
        Mongo.userCollection.deleteOne(Filters.eq("_id", old.id))
    }
}