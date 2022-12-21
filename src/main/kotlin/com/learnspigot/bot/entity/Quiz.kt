package com.learnspigot.bot.entity

class Quiz(
    val id: String,
    override val title: String,
    val passPercentage: Int,
) : CourseContent(title) {
    val url = "https://www.udemy.com/course/develop-minecraft-plugins-java-programming/learn/quiz/$id"
}
