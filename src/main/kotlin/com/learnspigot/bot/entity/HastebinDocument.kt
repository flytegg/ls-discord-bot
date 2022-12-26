package com.learnspigot.bot.entity

class HastebinDocument(val id: String, val data: String?) {
    fun getUrl(): String {
        return "https://paste.learnspigot.com/$id"
    }
}