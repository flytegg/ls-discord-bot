package com.learnspigot.bot.database.counting

data class Counting(
    val highestCount: Int = 0,
    val currentCount: Int = 0,
    val serverTotalCounts: Int = 0
)