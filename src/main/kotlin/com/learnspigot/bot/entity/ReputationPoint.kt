package com.learnspigot.bot.entity

import dev.morphia.annotations.Entity
import java.time.Instant

// TODO: Fix timestamps
@Entity
data class ReputationPoint(
    val epochTimestamp: Long,
    val fromMemberId: String?,
    val postId: String?
) {
    fun timestamp(): Instant = Instant.ofEpochMilli(epochTimestamp)

}