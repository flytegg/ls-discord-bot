package com.learnspigot.bot.dbmigrator.http

import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpRequest


class UdemyService : HttpService() {

    companion object {
        const val ENDPOINT = "https://www.udemy.com/api-2.0"
    }

    fun getUdemyUrlById(id: String): String {
        if(id == "0") return "https://www.udemy.com/null"
        with(sendJsonRequest(buildRequest("$ENDPOINT/users/$id"))) {
            val data = body().asJsonObject
            return "https://www.udemy.com${data["url"]?.asString ?: "/null"}"
        }
    }

    override fun buildRequest(url: String): HttpRequest {
        return try {
            HttpRequest.newBuilder()
                .uri(URI(url))
                .GET()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer ${System.getenv("UDEMY_BEARER")!!}")
                .setHeader("Cookie", "client_id=${System.getenv("UDEMY_CLIENT_ID")!!}; access_token=${System.getenv("UDEMY_BEARER")!!}")
                .build()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

}