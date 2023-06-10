package com.learnspigot.bot.util

import com.learnspigot.bot.Bot
import com.learnspigot.bot.Environment
import net.dv8tion.jda.api.entities.emoji.Emoji

object Server {

    private val jda = Bot.jda

    val guild = jda.getGuildById(Environment.get("GUILD_ID"))!!

    val managerRole = guild.getRoleById(Environment.get("MANAGEMENT_ROLE_ID"))!!

    val starboardChannel = guild.getTextChannelById(Environment.get("STARBOARD_CHANNEL_ID"))!!

    val nostarboardEmoji = Emoji.fromCustom("nostarboard", Environment.get("NOSTARBOARD_EMOJI_ID").toLong(), false)
    val starEmoji = Emoji.fromUnicode("⭐")
}