package com.learnspigot.bot.entity

data class UdemyProfile(
    val id: String,
    val url: String,
    val username: String,
    val ownedCourses: List<Course>
) {
    data class Course(val id: String, val title: String, val url: String,)
}
