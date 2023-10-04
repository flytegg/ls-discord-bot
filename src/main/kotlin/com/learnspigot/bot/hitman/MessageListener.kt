package com.learnspigot.bot.hitman

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageListener : ListenerAdapter() {

    val idiotGrammerFixerBroLikeActuallyStfuOngUCantSpellDumbAss = hashSetOf(
        "I m",
        "I ma",
    )

    val theIdiot = "1071963283332018177"

    override fun onMessageReceived(event: MessageReceivedEvent) {
        println(event.message.contentRaw)
        if (event.author.id == theIdiot) {
            if (idiotGrammerFixerBroLikeActuallyStfuOngUCantSpellDumbAss.any { event.message.contentRaw.contains(it, true) }) {
                event.channel.sendMessage("<@${theIdiot}> I'm*").queue()
                event.message.delete().queue()
            }
        }
    }

}