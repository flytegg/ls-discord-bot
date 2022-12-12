package com.learnspigot.bot.entity

data class Lecture(
    val id: String,
    val title: String,
    val description: String,
) {
    val url = "https://www.udemy.com/course/develop-minecraft-plugins-java-programming/learn/lecture/$id"
}
