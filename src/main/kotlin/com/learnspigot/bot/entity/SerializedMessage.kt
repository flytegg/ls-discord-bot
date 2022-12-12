package com.learnspigot.bot.entity

import dev.morphia.annotations.Entity
import net.dv8tion.jda.api.entities.Message

@Entity
data class SerializedMessage(
    val id: String,
    val content: String,
    val timestamp: Long,
    val channelId: String,
) {

    companion object {
        fun fromDiscordMessage(message: Message): SerializedMessage = SerializedMessage(
            message.id,
            message.contentRaw,
            message.timeCreated.toEpochSecond(),
            message.channel.id
        )
    }
}