package com.learnspigot.bot.entity

import dev.morphia.annotations.Entity
import dev.morphia.annotations.Id
import java.time.Instant

@Entity("giveaways")
data class Giveaway(
    @Id val id: String,
    val channelId: String = "",
    val prize: String = "Nothing",
    val endTime: Long = 1,
    val winnerAmount: Int = 1,
    val host: String = "",
    val entries: MutableSet<String> = mutableSetOf()
) {
    fun timestamp(): Instant = Instant.ofEpochMilli(endTime)
    fun entryTrigger(id: String): Boolean {
        return if(entries.contains(id)) {
            entries.remove(id)
            false
        } else {
            entries.add(id)
            true
        }
    }
}