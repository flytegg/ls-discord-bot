package com.learnspigot.bot.http

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import javax.net.ssl.SSLSession

open class HttpService {
  private val client: HttpClient = HttpClient.newHttpClient()

  fun sendJsonRequest(request: HttpRequest?): HttpResponse<JsonElement> {
    return convertResponseToJson(sendStringRequest(request))
  }

  fun sendStringRequest(request: HttpRequest?): HttpResponse<String> {
    val response: HttpResponse<String> = try {
      client.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (e: IOException) {
      throw RuntimeException(e)
    } catch (e: InterruptedException) {
      throw RuntimeException(e)
    }
    return response
  }

  fun sendUnsafeStringRequest(request: HttpRequest?): HttpResponse<String>? {
    val response: HttpResponse<String> = try {
      client.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (e: IOException) {
      return null
    } catch (e: InterruptedException) {
      return null
    }
    return response
  }

  fun convertResponseToJson(response: HttpResponse<String>): HttpResponse<JsonElement> {
    return object : HttpResponse<JsonElement> {
      override fun statusCode(): Int {
        return response.statusCode()
      }

      override fun request(): HttpRequest {
        return response.request()
      }

      override fun previousResponse(): Optional<HttpResponse<JsonElement>> {
        return if (response.previousResponse().isEmpty) {
          Optional.empty()
        } else {
          convertResponseToJson(response.previousResponse().get()).previousResponse()
        }
      }

      override fun headers(): HttpHeaders {
        return response.headers()
      }

      override fun body(): JsonObject {
        return Gson().fromJson(if (response.body() == "") "{}" else response.body(), JsonObject::class.java)
      }

      override fun sslSession(): Optional<SSLSession> {
        return response.sslSession()
      }

      override fun uri(): URI {
        return response.uri()
      }

      override fun version(): HttpClient.Version {
        return response.version()
      }
    }
  }

  open fun buildRequest(url: String): HttpRequest {
    return try {
      HttpRequest.newBuilder()
        .uri(URI(url))
        .GET()
        .build()
    } catch (e: URISyntaxException) {
      throw RuntimeException(e)
    }
  }

  open fun buildPost(url: String, payload: String): HttpRequest {
    return try {
      HttpRequest.newBuilder()
        .uri(URI(url))
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build()
    } catch (e: URISyntaxException) {
      throw RuntimeException(e)
    }
  }

}
