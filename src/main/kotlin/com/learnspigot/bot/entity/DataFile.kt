package com.learnspigot.bot.entity

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class DataFile(
    var migrated: Boolean = false,
    var migratedRep: Boolean = false,
    val bearer: String = System.getenv("UDEMY_BEARER"),
    var allTimeLeaderboards: List<String> = emptyList(),
    var monthlyLeaderboards: List<String> = emptyList(),
    var lookupMessages: List<String> = emptyList()
)
