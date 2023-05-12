package com.learnspigot.bot.lecture

data class Lecture(
    val id: String,
    val title: String,
    val description: String) {

    fun url(): String {
        return "https://www.udemy.com/course/develop-minecraft-plugins-java-programming/learn/lecture/$id"
    }

}