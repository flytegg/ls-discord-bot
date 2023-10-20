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
import java.util.concurrent.CompletableFuture

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

        val rawLinks = getBinLinks(event.message.contentRaw) ?: return
        if (rawLinks.size > 6) { // No reason for someone to be sending over SIX pastebins (probably)
            event.channel.sendMessageEmbeds(PasteCommand.getNewPasteBinEmbed()).queue()
            return
        }
        val lsLinks = convertToLSBins(rawLinks).takeIf { it.isNotEmpty() } ?: return
        val description = StringBuilder()
            .appendLine("We highly recommend using our custom pastebin next time you need to paste some code. Your paste will never expire!")
            .appendLine()
            .appendLines(lsLinks.map { "${Server.rightarrowEmoji.asMention} $it" })

        event.message.suppressEmbeds(true).queue()

        event.channel.sendMessageEmbeds(
            embed()
                .setTitle("Converted to LearnSpigot pastebin")
                .setDescription(description)
                .setFooter("PS: If you ever forget the link to the website, just run /pastebin.").build()
        ).queue()
    }

    private fun StringBuilder.appendLines(lines: List<String>) = lines.forEach(::appendLine).let { this }

    private val regex = "https?://(?:${KNOWN_PASTEBINS.joinToString("|")})/([a-zA-Z0-9]+)".toRegex()
    private fun String.toUrlRaw() = lastIndexOf("/").takeIf { it != -1 }?.let { index -> replaceRange(index, index +1, "/raw/") }

    private fun getBinLinks(rawMessage: String): List<String>? = regex
        .findAll(rawMessage)
        .toList()
        .mapNotNull { it.value.toUrlRaw() }
        .takeIf { it.isNotEmpty() }


    private fun startLSBinConversion(client: HttpClient, link: String) = CompletableFuture.supplyAsync {
        val rawText = client.send(HttpRequest.newBuilder().uri(URI.create(link)).build(), HttpResponse.BodyHandlers.ofString()).body()
        val urlRequest = HttpRequest.newBuilder()
            .uri(URI.create("$LS_PASTEBIN/documents"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(rawText))
            .build()

        runCatching {
            val response = client.send(urlRequest, HttpResponse.BodyHandlers.ofString()).takeIf { it.statusCode() == 200 }
                ?: return@runCatching null
            val keyObject = GSON.fromJson(response.body(), JsonObject::class.java)
            return@runCatching "$LS_PASTEBIN/${keyObject.get("key")?.asString}"
        }
            .onFailure { it.printStackTrace() }
            .getOrNull()
    }

    private fun convertToLSBins(links: List<String>): List<String> {
        val client = HttpClient.newHttpClient()
        val futures = links.map { startLSBinConversion(client, it) }
        CompletableFuture.allOf(*futures.toTypedArray()).join()
        val results = futures.mapNotNull { it.join() }
        if (results.size != futures.size) println("Conversion to LS Pastebin failed")
        return results
    }

}