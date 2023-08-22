package com.learnspigot.bot.intellijkey

import java.io.File

class IJUltimateKeyRegistry {

    private val csvFile = File("keys.csv")

    val keys = csvFile.readLines().toMutableList()

    fun getKey(): String? {
        return keys.removeFirstOrNull()
    }

    fun readdKey(key: String) {
        keys.add(key)
    }

    fun removeKeyFromFile(key: String) {
        csvFile.writeText(keys.joinToString("\n"))
    }
}
