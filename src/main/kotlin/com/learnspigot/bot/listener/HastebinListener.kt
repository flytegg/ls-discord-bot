package com.learnspigot.bot.listener

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.http.HastebinService
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.stream.Collectors

class HastebinListener(bot: JDA) {
    init {
        bot.listener<MessageReceivedEvent> {
            if (it.author.isBot) return@listener
            val match = Regex("(https?://hastebin\\.com)/([0-9a-zA-Z]+)")
            val matches = match.findAll(it.message.contentStripped).toList()
            if (matches.isEmpty()) return@listener

//            println(matches[0].groups[0])
            val hastebinService = HastebinService()
            val newBinUrls = matches.mapIndexed() { index, matchGroup ->
                val origin = matchGroup.groups[1]!!.value
                val id = matchGroup.groups[2]!!.value
                val url = "$origin/documents/$id"

                val newBin = hastebinService.createDocument("https://paste.learnspigot.com/documents",hastebinService.readDocument(url).data!!)

                return@mapIndexed newBin
            }

            it.message.replyEmbeds(Embed {
                description = "Learn Spigot has [its own Hastebin server](<https://paste.learnspigot.com/>)! Any pastes posted on it will never expire, so please post there in the future. In the meantime, I've re-uploaded your linked pastes on our server."
                color = LearnSpigotBot.EMBED_COLOR
                field {
                    name = "Re-uploaded Pastes"
                    value = newBinUrls.stream().map { data -> "\u2022 [${data.getUrl()}](<${data.getUrl()}>)" }.collect(Collectors.joining("\n"))
                }
            }).queue()
            println(newBinUrls.toString())
        }
    }
}
