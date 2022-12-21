package com.learnspigot.bot.entity

class Lecture(
    val id: String,
    override val title: String,
    val description: String,
) : CourseContent(title) {
    val url = "https://www.udemy.com/course/develop-minecraft-plugins-java-programming/learn/lecture/$id"
}
