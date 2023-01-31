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
            val hastebinService = HastebinService()

            val hastebinUrls = hastebinService.findHastebinDocuments(it.message.contentStripped)

            if (hastebinUrls.isEmpty()) return@listener

            val reuploadedHastebinUrls = hastebinUrls
                .mapNotNull { hastebinService.reuploadHastebin(it) } // add reuplodaed hastebin urls to a list

            it.message.replyEmbeds(Embed {
                description = "We have [our own Hastebin server](<https://paste.learnspigot.com/>)! Posts there will never be deleted. In the meantime, I've re-uploaded your linked pastes on our server."
                color = LearnSpigotBot.EMBED_COLOR
                field {
                    name = "Re-uploaded Pastes"
                    value =
                        reuploadedHastebinUrls.joinToString("\n") { data -> "\u2022 [${data.getUrl()}](<${data.getUrl()}>)" }
                }
            }).queue()
        }
    }
}
