package com.learnspigot.bot.starboard

import com.learnspigot.bot.util.Server
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion

object StarboardUtil {
    fun Message.getEmojiReactionCount(emoji: Emoji): Int = this.getReaction(emoji)?.count ?: 0
}