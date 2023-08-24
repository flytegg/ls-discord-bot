package com.learnspigot.bot.help

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.regex.Pattern

class HastebinListener : ListenerAdapter() {

    private val GSON = Gson()

    private val KNOWN_PASTEBINS = listOf(
        "pastebin.com",
        "paste.md-5.net",
        "paste.helpch.at"
    )

    private val LS_PASTEBIN = "https://paste.learnspigot.com"

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (event.author.isBot) return
        if (event.guildChannel.asThreadChannel().parentChannel.id != Server.helpChannel.id) return

        val rawLink = getBinLink(event.message.contentRaw) ?: return
        val rawText = getRawText(rawLink) ?: return
        val newPasteLink = uploadPaste(rawText) ?: return

        event.message.suppressEmbeds(true).queue()

        event.channel.sendMessageEmbeds(
            embed()
                .setTitle("Converted to LearnSpigot pastebin")
                .setDescription("" +
                        "We highly recommend using our custom pastebin next time you need to paste some code. Your paste will never expire!" +
                        "\n" +
                        "\n<:right:1051865413216120853> $newPasteLink")
                .setFooter("PS: If you ever forget the link to the website, just run /pastebin.").build()
        ).queue()
    }

    private fun getBinLink(rawMessage: String): String? {
        val pattern = Pattern.compile("https?://(?:${KNOWN_PASTEBINS.joinToString("|")})/([a-zA-Z0-9]+)")
        val matcher = pattern.matcher(rawMessage)
        return if (matcher.find()) {
            val url = matcher.group()
            val lastIndex = url.lastIndexOf("/")
            if (lastIndex != -1) {
                val rawUrl = StringBuilder(url)
                rawUrl.replace(lastIndex, lastIndex + 1, "/raw/")
                rawUrl.toString()
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun getRawText(rawLink: String): String? {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(rawLink))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun uploadPaste(content: String): String? {
        val client = HttpClient.newHttpClient()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$LS_PASTEBIN/documents"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(content))
            .build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val keyObject = GSON.fromJson(response.body(), JsonObject::class.java)
                val pasteUrl = "$LS_PASTEBIN/${keyObject.get("key")?.asString}"
                pasteUrl
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}