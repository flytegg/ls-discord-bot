package com.learnspigot.bot.notice

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class NoticeListener : ListenerAdapter() {

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        if (event.member == null) return
        val notice = when(event.name) {
            "Help Notice" -> Notice.HELP
            else -> return
        }

        val reply = event.deferReply()
        notice.reply(reply, event.target.author.idLong)
        reply.queue()
    }

}