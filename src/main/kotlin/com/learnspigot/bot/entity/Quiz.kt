package com.learnspigot.bot.entity

data class Quiz(
    val id: String,
    val title: String,
    val passPercentage: Int,
) {
    val url = "https://www.udemy.com/course/develop-minecraft-plugins-java-programming/learn/quiz/$id"
}
