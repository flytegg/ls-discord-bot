package com.learnspigot.bot

import io.github.cdimascio.dotenv.Dotenv

object Environment {
    private val dotenv = Dotenv.configure()
        .systemProperties()
        .load()

    val MONGO_URI get() = get("MONGO_URI")
    val MONGO_DATABASE get() = get("MONGO_DATABASE")
    val BOT_TOKEN get() = get("BOT_TOKEN")
    val GUILD_ID get() = get("GUILD_ID")
    val SUGGESTIONS_CHANNEL_ID get() = get("SUGGESTIONS_CHANNEL_ID")
    val LEADERBOARD_CHANNEL_ID get() = get("LEADERBOARD_CHANNEL_ID")
    val VERIFY_CHANNEL_ID get() = get("VERIFY_CHANNEL_ID")
    val HELP_CHANNEL_ID get() = get("HELP_CHANNEL_ID")
    val QUESTIONS_CHANNEL_ID get() = get("QUESTIONS_CHANNEL_ID")
    val GET_COURSE_CHANNEL_ID get() = get("GET_COURSE_CHANNEL_ID")
    val SUPPORT_CHANNEL_ID get() = get("SUPPORT_CHANNEL_ID")
    val GENERAL_CHANNEL_ID get() = get("GENERAL_CHANNEL_ID")
    val MANAGER_CHANNEL_ID get() = get("MANAGER_CHANNEL_ID")
    val KNOWLEDGEBASE_CHANNEL_ID get() = get("KNOWLEDGEBASE_CHANNEL_ID")
    val PROJECTS_CHANNEL_ID get() = get("PROJECTS_CHANNEL_ID")
    val STARBOARD_CHANNEL_ID get() = get("STARBOARD_CHANNEL_ID")
    val SHOWCASE_CHANNEL_ID get() = get("SHOWCASE_CHANNEL_ID")
    val NEWS_CHANNEL_ID get() = get("NEWS_CHANNEL_ID")
    val COUNTING_CHANNEL_ID get() = get("COUNTING_CHANNEL_ID")
    val VOICE_CHANNEL_ID get() = get("VOICE_CHANNEL_ID")
    val CHAT_CATEGORY get() = get("CHAT_CATEGORY")
    val STUDENT_ROLE_ID get() = get("STUDENT_ROLE_ID")
    val SUPPORT_ROLE_ID get() = get("SUPPORT_ROLE_ID")
    val STAFF_ROLE_ID get() = get("STAFF_ROLE_ID")
    val MANAGEMENT_ROLE_ID get() = get("MANAGEMENT_ROLE_ID")
    val VERIFIER_ROLE_ID get() = get("VERIFIER_ROLE_ID")
    val RIGHT_ARROW_EMOJI_ID get() = get("RIGHT_ARROW_EMOJI_ID")
    val UPVOTE_EMOJI_ID get() = get("UPVOTE_EMOJI_ID")
    val DOWNVOTE_EMOJI_ID get() = get("DOWNVOTE_EMOJI_ID")
    val NOSTARBOARD_EMOJI_ID get() = get("NOSTARBOARD_EMOJI_ID")
    val STARBOARD_AMOUNT get() = get("STARBOARD_AMOUNT")


    private fun get(variable: String): String {
        return dotenv.get(variable)
    }
}