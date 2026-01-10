package com.learnspigot.bot.videos.youtube

import com.learnspigot.bot.videos.WordMatcher

class YouTubeRegistry {
    private val videos = mutableListOf<YouTubeItem>()
    private val matcher: WordMatcher = WordMatcher()

    fun findVideos(query: String, amount: Int): MutableList<YouTubeItem> {
        return matcher.getTopMatchesBy(query, videos, amount) { it.title }.toMutableList()
    }

    init {
        videos.addAll(
            mutableListOf(
                YouTubeItem(
                    "dQw4w9WgXcQ",
                    "Sample Video 1",
                    "This is a sample video description."
                ),
                YouTubeItem(
                    "9bZkp7q19f0",
                    "Sample Video 2",
                    "Another sample video description."
                )
            )
        )
    }
}
