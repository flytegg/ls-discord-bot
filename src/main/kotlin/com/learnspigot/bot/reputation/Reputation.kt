package com.learnspigot.bot.reputation

import org.bson.Document
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

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

    fun isThisMonth(): Boolean = YearMonth.now().atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC).isBefore(Instant.ofEpochSecond(timestamp))
}
