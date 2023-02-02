package com.learnspigot.bot.util

enum class KnowledgeBaseTypes(val lowercaseName: String, val lowerbound: Int, val upperbound: Int) {
    TUTORIAL("tutorial", 0, 5),
    PROJECT("project", 0, 10)
}