package com.learnspigot.bot.index

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object Http {
    private val client: HttpClient =
        HttpClient
            .newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build()

    private val gson: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

    fun <T> getJson(
        url: String,
        clazz: Class<T>,
    ): T? {
        val req =
            HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build()
        val res = client.send(req, HttpResponse.BodyHandlers.ofInputStream())
        if (res.statusCode() != 200 || res.body() == null) return null
        return runCatching {
            gson.fromJson(InputStreamReader(res.body()), clazz)
        }.getOrNull()
    }

    fun getPlain(url: String): InputStream? {
        val req =
            HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .header("Accept", "text/plain")
                .GET()
                .build()
        val res = client.send(req, HttpResponse.BodyHandlers.ofInputStream())
        return res.body()
    }
}
