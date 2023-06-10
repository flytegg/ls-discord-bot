package com.learnspigot.bot.starboard

import com.learnspigot.bot.util.Server
import org.bson.Document


data class StarboardEntry(val originalMessageId: String, val startboardMessageId: String) {
    fun document(): Document {
        val document = Document()
        document["originalMessageId"] = originalMessageId
        document["startboardMessageId"] = startboardMessageId

        return document
    }

    companion object {
        fun fromDocument(document: Document): StarboardEntry = StarboardEntry(
            originalMessageId = document.getString("originalMessageId"),
            startboardMessageId = document.getString("startboardMessageId")
        )
    }
}