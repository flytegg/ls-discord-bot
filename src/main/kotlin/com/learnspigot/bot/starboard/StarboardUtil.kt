package com.learnspigot.bot.starboard

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji

object StarboardUtil {
    fun Message.getEmojiReactionCount(emoji: Emoji): Int = this.getReaction(emoji)?.count ?: 0
}