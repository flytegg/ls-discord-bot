package com.learnspigot.bot.help.stats

import org.bson.Document

/**
 * Stored stats for one help post. Times are epoch seconds.
 */
data class HelpPost(
    val id: String,
    val ownerId: String,
    val createdAt: Long,
    var closedAt: Long? = null, // Only set when closed with /close (archived and locked)
    var messageCount: Int = 0,
    var firstResponseAt: Long? = null // First message from someone other than the poster or a bot
) {

    fun document(): Document = Document()
        .append("_id", id)
        .append("ownerId", ownerId)
        .append("createdAt", createdAt)
        .append("closedAt", closedAt)
        .append("messageCount", messageCount)
        .append("firstResponseAt", firstResponseAt)

    companion object {
        fun fromDocument(document: Document) = HelpPost(
            document.getString("_id"),
            document.getString("ownerId"),
            document.getLong("createdAt"),
            document.getLong("closedAt"),
            document.getInteger("messageCount", 0),
            document.getLong("firstResponseAt")
        )
    }
}
