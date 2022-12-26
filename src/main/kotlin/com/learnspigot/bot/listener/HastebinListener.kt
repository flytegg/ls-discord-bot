package com.learnspigot.bot.listener

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.HastebinDocument
import com.learnspigot.bot.http.HastebinService
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.InlineEmbed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class HastebinListener(bot: JDA) {
    init {
        bot.listener<MessageReceivedEvent> {
            if (it.author.isBot) return@listener
            val match = Regex("(https?://hastebin\\.com)/([0-9a-zA-Z]+)")
            val matches = match.findAll(it.message.contentStripped).toList()
            if (matches.isEmpty()) return@listener

//            println(matches[0].groups[0])
            val hastebinService = HastebinService()
            val newBins = mutableListOf<String>()

            matches.mapIndexedTo(newBins) { index, matchGroup ->
                val origin = matchGroup.groups[1]!!.value
                val id = matchGroup.groups[2]!!.value
                val url = "$origin/documents/$id"
                return@mapIndexedTo hastebinService.readDocument(url).data!!
            }
            
            it.message.reply("${newBins.}")

            val embed: InlineEmbed = InlineEmbed({
                title = "Created Hastebin"
                description = "You can view the hastebin here: ${url}"
                color = LearnSpigotBot.EMBED_COLOR
            })


            it.message.replyEmbed(MessageEmbed(title = "Hi")).queue()
            println(newBins.toString())
//            val hastebinService = HastebinService()
//            val url = it.groups[0]!!.value
//            println(url)
//            println(hastebinService.readDocument(url))
        }
    }
//            println("Tried to match message: ${match.findAll(it.message.contentStripped).toList()[0].groups[1]}")
//            if(it.message.contentStripped.contains(Regex("https?://hastebin\\.com/([0-9a-zA-Z]+)")))
}
