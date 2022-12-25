package com.learnspigot.bot.http

class HastebinService {
    fun createDocument(data: String): String {
        val httpService = HttpService()
        val post = httpService.buildPost("https://paste.learnspigot.com/documents", data)
        val result = httpService.convertResponseToJson(httpService.sendStringRequest(post)).body().asJsonObject["key"].asString
        return result
    }
}