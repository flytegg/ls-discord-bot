package com.learnspigot.bot.http

import com.learnspigot.bot.entity.HastebinDocument

class HastebinService {
  private val httpService = HttpService()

  fun uploadHastebin(site: String, data: String): HastebinDocument? {
    val post = httpService.buildPost("$site/documents", data)
    val key = httpService.convertResponseToJson(httpService.sendStringRequest(post)).body().asJsonObject["key"].asString
      ?: return null
    return HastebinDocument(site = site, key = key, data = data)
  }

  fun fetchHastebin(site: String, key: String): HastebinDocument? {
    val get = httpService.buildRequest("$site/documents/$key")
    val response = httpService.sendUnsafeStringRequest(get)
    if (response == null || response.statusCode() != 200) return null

    val data = httpService.convertResponseToJson(response).body()?.asJsonObject

    if (data == null || data.get("key")?.asString.isNullOrBlank() || data.get("data")?.asString.isNullOrEmpty()) return null
    return HastebinDocument(site = site, key = data["key"].asString, data = data["data"].asString)
  }

  fun findHastebinDocuments(message: String): List<HastebinDocument> {
//    val match = Regex("(https?://paste\\..+\\..{2,63})/(.*?)(\\.|\\s|\$)")
    val regexes = listOf(
      "paste\\..+?\\..{2,63}?",
      "hastebin\\.com",
    )
    val matches = regexes.flatMap { regex ->
      Regex("(https?://$regex)/(.+?)(\\.|\\s|\$)").findAll(message)
    }.filter { match -> match.groups[1]!!.value != "https://paste.learnspigot.com" }.toList()

    val uniqueMatches = matches.distinctBy {Pair(it.groups[1]?.value, it.groups[2]?.value)}.toList()

    return uniqueMatches.mapNotNull { match -> fetchHastebin(site = match.groups[1]!!.value, key = match.groups[2]!!.value) }
  }


  fun reuploadHastebin(document: HastebinDocument): HastebinDocument? {
    return uploadHastebin("https://paste.learnspigot.com", document.data)
  }
}