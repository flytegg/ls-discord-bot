package com.learnspigot.bot.suggestion.grammar

import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageListener : ListenerAdapter() {

    private val idiotGrammerFixerBroLikeActuallyStfuOngUCantSpellDumbAss = hashSetOf(
        "I m ",
        "I ma ",
    )

    private val theIdiot = "1071963283332018177"

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.id == theIdiot) {
            if (idiotGrammerFixerBroLikeActuallyStfuOngUCantSpellDumbAss.any { event.message.contentRaw.contains(it, true) }) {
                event.channel.sendMessage("<@${theIdiot}> I'm*").queue() {
                    it.addReaction(Emoji.fromFormatted(":regional_indicator_l:")).queue()
                }

                event.message.delete().queue()
            }
        }
    }

}