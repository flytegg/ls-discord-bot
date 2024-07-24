package com.learnspigot.bot

import com.learnspigot.bot.Bot.jda
import net.dv8tion.jda.api.entities.emoji.Emoji

object Server {

    val guildId = Environment.GUILD_ID
    val guild = jda.getGuildById(Environment.GUILD_ID)!!

    val managementRole = guild.getRoleById(Environment.MANAGEMENT_ROLE_ID)!!

    val leaderboardChannel = guild.getTextChannelById(Environment.LEADERBOARD_CHANNEL_ID)!!
    val verifyChannel = guild.getTextChannelById (Environment.VERIFY_CHANNEL_ID)!!
    val managerChannel = guild.getTextChannelById(Environment.MANAGER_CHANNEL_ID)!!
    val starboardChannel = guild.getTextChannelById(Environment.STARBOARD_CHANNEL_ID)!!
    val helpChannel = guild.getForumChannelById(Environment.HELP_CHANNEL_ID)!!
    val knowledgebaseChannel = guild.getForumChannelById(Environment.KNOWLEDGEBASE_CHANNEL_ID)!!
    val countingChannel = guild.getTextChannelById(Environment.COUNTING_CHANNEL_ID)!!

    val starEmoji = Emoji.fromUnicode("⭐")
    val upvoteEmoji = Emoji.fromCustom("upvote", Environment.UPVOTE_EMOJI_ID.toLong(), false)
    val downvoteEmoji = Emoji.fromCustom("downvote", Environment.DOWNVOTE_EMOJI_ID.toLong(), false)
    val nostarboardEmoji = Emoji.fromCustom("nostarboard", Environment.NOSTARBOARD_EMOJI_ID.toLong(), false)
    val rightEmoji = Emoji.fromCustom("right", Environment.RIGHT_ARROW_EMOJI_ID.toLong(), false)
}