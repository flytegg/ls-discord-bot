package com.learnspigot.bot

import net.dv8tion.jda.api.entities.emoji.Emoji

object Server {

    private val jda = Bot.jda

    val guildId = Environment.get("GUILD_ID")
    val guild = jda.getGuildById(Environment.get("GUILD_ID"))!!

    val managementRole = guild.getRoleById(Environment.get("MANAGEMENT_ROLE_ID"))!!

    val leaderboardChannel = guild.getTextChannelById(Environment.get("LEADERBOARD_CHANNEL_ID"))!!
    val verifyChannel = guild.getTextChannelById(Environment.get("VERIFY_CHANNEL_ID"))!!
    val managerChannel = guild.getTextChannelById(Environment.get("MANAGER_CHANNEL_ID"))!!
    val starboardChannel = guild.getTextChannelById(Environment.get("STARBOARD_CHANNEL_ID"))!!
    val helpChannel = guild.getForumChannelById(Environment.get("HELP_CHANNEL_ID"))!!
    val knowledgebaseChannel = guild.getForumChannelById(Environment.get("KNOWLEDGEBASE_CHANNEL_ID"))!!
    val countingChannel = guild.getTextChannelById(Environment.get("COUNTING_CHANNEL_ID"))!!
    val workshopChannel = guild.getForumChannelById(Environment.get("WORKSHOP_CHANNEL_ID"))!!

    val upvoteEmoji = Emoji.fromCustom("upvote", Environment.get("UPVOTE_EMOJI_ID").toLong(), false)
    val downvoteEmoji = Emoji.fromCustom("downvote", Environment.get("DOWNVOTE_EMOJI_ID").toLong(), false)
    val nostarboardEmoji = Emoji.fromCustom("nostarboard", Environment.get("NOSTARBOARD_EMOJI_ID").toLong(), false)
    val rightEmoji = Emoji.fromCustom("right", Environment.get("RIGHT_ARROW_EMOJI_ID").toLong(), false)
    val starEmoji = Emoji.fromUnicode("⭐")
}