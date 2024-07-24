package com.learnspigot.bot.database.counting

import com.google.gson.annotations.SerializedName
import org.bson.types.ObjectId

data class Counting(
    @SerializedName("_id") val id: ObjectId = ObjectId(),
    val highestCount: Int = 0,
    val currentCount: Int = 0,
    val serverTotalCounts: Int = 0
)