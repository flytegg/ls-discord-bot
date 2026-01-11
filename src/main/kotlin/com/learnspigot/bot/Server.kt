package com.learnspigot.bot

import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji

object Server {

    private inline val jda get() = Bot.jda
    private val dotenv = Dotenv.configure().systemProperties().load()

    private fun get(variable: String): String = dotenv.get(variable)

    val GUILD_ID = get("GUILD_ID")
    val GUILD = jda.getGuildById(get("GUILD_ID"))!!

    val ROLE_MANAGEMENT = GUILD.getRoleById(get("MANAGEMENT_ROLE_ID"))!!
    val ROLE_STAFF = GUILD.getRoleById(get("STAFF_ROLE_ID"))!!
    val ROLE_SUPPORT = GUILD.getRoleById(get("SUPPORT_ROLE_ID"))!!
    val ROLE_STUDENT = GUILD.getRoleById(get("STUDENT_ROLE_ID"))!!
    val ROLE_VERIFIER = GUILD.getRoleById(get("VERIFIER_ROLE_ID"))!!

    val CHANNEL_LEADERBOARD = GUILD.getTextChannelById(get("LEADERBOARD_CHANNEL_ID"))!!
    val CHANNEL_VERIFY = GUILD.getTextChannelById(get("VERIFY_CHANNEL_ID"))!!
    val CHANNEL_MANAGER = GUILD.getTextChannelById(get("MANAGER_CHANNEL_ID"))!!
    val CHANNEL_STARBOARD = GUILD.getTextChannelById(get("STARBOARD_CHANNEL_ID"))!!
    val CHANNEL_HELP = GUILD.getForumChannelById(get("HELP_CHANNEL_ID"))!!
    val CHANNEL_KNOWLEDGEBASE = GUILD.getForumChannelById(get("KNOWLEDGEBASE_CHANNEL_ID"))!!
    val CHANNEL_COUNTING = GUILD.getTextChannelById(get("COUNTING_CHANNEL_ID"))!!
    val CHANNEL_SUPPORT = GUILD.getTextChannelById(get("SUPPORT_CHANNEL_ID"))!!
    val CHANNEL_KEYLOG = GUILD.getTextChannelById(get("KEYLOG_CHANNEL_ID"))
    val CHANNEL_PROJECTS = GUILD.getTextChannelById(get("PROJECTS_CHANNEL_ID"))!!
    
    val EMOJI_UPVOTE = Emoji.fromCustom("upvote", get("UPVOTE_EMOJI_ID").toLong(), false)
    val EMOJI_DOWNVOTE = Emoji.fromCustom("downvote", get("DOWNVOTE_EMOJI_ID").toLong(), false)
    val EMOJI_NO_STARBOARD = Emoji.fromCustom("nostarboard", get("NOSTARBOARD_EMOJI_ID").toLong(), false)
    val EMOJI_RIGHT_ARROW = Emoji.fromCustom("right", get("RIGHT_ARROW_EMOJI_ID").toLong(), false)
    val EMOJI_STAR = Emoji.fromUnicode("⭐")

    val STARBOARD_AMOUNT = get("STARBOARD_AMOUNT").toInt()

    inline val Member.isManager: Boolean get() = roles.contains(ROLE_MANAGEMENT)
    inline val Member.isStaff: Boolean get() = roles.contains(ROLE_STAFF) || isManager
    inline val Member.isSupport: Boolean get() = roles.contains(ROLE_SUPPORT) || isStaff
    inline val Member.isVerifier: Boolean get() = roles.contains(ROLE_VERIFIER)|| isSupport
    inline val Member.isStudent: Boolean get() = roles.contains(ROLE_STUDENT) || isSupport

    fun Member.owns(channel: ThreadChannel): Boolean = idLong == channel.ownerIdLong
    fun Channel.isChannel(other: Channel) = idLong == other.idLong
}