package com.learnspigot.bot.crazy

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CrazyListener : ListenerAdapter() {

  override fun onMessageReceived(e: MessageReceivedEvent) {
    if (e.author.isBot) return
    if (!e.isFromGuild) return
    if (e.guild.id != Server.guildId) return
    if (e.channel == Server.suggestionsChannel) return

    val message = e.message.contentStripped

    println(message)

    if (message.contains("crazy", true)) {
      return e.message.reply("crazy? I was crazy once...").queue()
    } else if (message.contains("they locked me in a room", true)) {
      e.message.reply("a rubber room, a rubber room with rats").queue()
    } else if (message.contains("rats? rats make me crazy")) {
      e.message.reply("crazy? I was crazy once...").queue()
    } else if (message.contains("rats?", true)) {
      e.message.reply("rats make my crazy.").queue()
      e.message.channel.sendMessage("crazy? I was crazy once...").queue()
    }
  }

}