package com.learnspigot.bot.reference

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.index.IndexEntry
import com.learnspigot.bot.util.isChannel
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ReferenceListener : ListenerAdapter() {

    val code = Regex("(?<!\\\\)`([^`]+)`")

    fun parseInlineCode(input: String) = code
        .findAll(input).map { it.groupValues[1] }.toList()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if(event.author.isBot||event.channelType!= ChannelType.GUILD_PUBLIC_THREAD||
            !event.channel.asThreadChannel().parentChannel.isChannel(Server.CHANNEL_HELP)) return

        val entries = mutableSetOf<IndexEntry>()

        for (s in parseInlineCode(event.message.contentRaw)) {
            Registry.INDEX.mappings.find { s.startsWith("${it.version}:") }?.apply {
                val entry = s.substringAfter(':')
                this.entries.find { it.name == entry || it.simpleName == entry }?.apply { entries.add(this) }
            } ?: run {
                Registry.INDEX.entries.find { it.name == s || it.simpleName == s }?.apply { entries.add(this) }
            }
        }

        if(entries.isEmpty())return
        var header = entries.size.coerceAtLeast(1)
        if(header>3)header = 0

        event.message.replyFormat("${"#".repeat(header)}${if(header!=0)" " else ""}${entries.joinToString(", ") { it.formatted }}")
            .queue()
    }

}