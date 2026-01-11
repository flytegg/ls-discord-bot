package com.learnspigot.bot.videos.youtube

data class YouTubeItem(
    val id: String,
    val title: String,
    val description: String
) {
    fun url(): String {
        return "https://www.youtube.com/watch?v=$id"
    }
}
