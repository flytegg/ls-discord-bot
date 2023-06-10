package com.learnspigot.bot.starboard

import com.learnspigot.bot.util.Server
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion

object StarboardUtil {
    fun isStarEmoji(emoji: EmojiUnion) = emoji == Server.starEmoji

    fun isNostarboardEmoji(emoji: EmojiUnion) = emoji == Server.nostarboardEmoji

    fun Message.getEmojiReactionCount(emoji: Emoji): Int = this.reactions.find { it.emoji == emoji }?.count ?: 0
}