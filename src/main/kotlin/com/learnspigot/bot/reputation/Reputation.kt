package com.learnspigot.bot.reputation

import org.bson.Document

data class Reputation(
    val timestamp: Long,
    val fromMemberId: String?,
    val fromPostId: String?
) {
    fun document(): Document {
        val document = Document()
        document["timestamp"] = timestamp
        if (fromMemberId != null) document["fromMemberId"] = fromMemberId
        if (fromPostId != null) document["fromPostId"] = fromPostId
        return document
    }
}
