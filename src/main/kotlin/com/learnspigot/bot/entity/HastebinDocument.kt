package com.learnspigot.bot.entity

data class HastebinDocument(val site: String, val key: String, val data: String) {
  fun getUrl (): String {
    return "$site/$key"
  }
}