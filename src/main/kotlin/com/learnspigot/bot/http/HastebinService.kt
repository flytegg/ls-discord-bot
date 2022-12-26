package com.learnspigot.bot.http

import com.learnspigot.bot.entity.HastebinDocument

class HastebinService {
    fun createDocument(data: String): HastebinDocument {
        val httpService = HttpService()
        val post = httpService.buildPost("https://paste.learnspigot.com/documents", data)
        val result = httpService.convertResponseToJson(httpService.sendStringRequest(post)).body().asJsonObject["key"].asString
        return HastebinDocument(result, data)
    }

    fun readDocument(url: String): HastebinDocument {
        val httpService = HttpService()
        val get = httpService.buildRequest(url)
        val _data = httpService.sendJsonRequest(get).body()
        println(_data)
        val data = httpService.convertResponseToJson(httpService.sendStringRequest(get)).body().asJsonObject
        return HastebinDocument(data["key"].asString, data["data"].asString)
    }
}