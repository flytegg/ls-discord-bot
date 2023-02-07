package com.learnspigot.bot.util

enum class KnowledgeBaseType(val displayName: String, val range: IntRange) {
    TUTORIAL("Tutorial", 0..5),
    PROJECT("Project", 0..10)
}