package com.learnspigot.bot

import com.learnspigot.bot.lecture.LectureRegistry
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.reputation.LeaderboardMessage
import com.learnspigot.bot.starboard.StarboardRegistry
import com.learnspigot.bot.util.Server
import com.learnspigot.bot.verification.VerificationMessage
import gg.flyte.neptune.Neptune
import gg.flyte.neptune.annotation.Instantiate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy

class Bot {
    private val profileRegistry = ProfileRegistry()

    init {
        jda = JDABuilder.createDefault(Environment.get("BOT_TOKEN"))
            .setActivity(Activity.watching("learnspigot.com"))
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
            )
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilter.ALL)
            .build()
            .awaitReady()

        run { Server } // intentional to initialize vals

        val guild = jda.getGuildById(Environment.get("GUILD_ID"))!!
        VerificationMessage(guild)
        LeaderboardMessage(guild, profileRegistry)

        Neptune.Builder(jda, this)
            .addGuilds(guild)
            .clearCommands(false)
            .registerAllListeners(true)
            .create()

    }

    @Instantiate
    fun profileRegistry(): ProfileRegistry {
        return profileRegistry
    }

    @Instantiate
    fun lectureRegistry(): LectureRegistry {
        return LectureRegistry()
    }

    @Instantiate
    fun starboardRegistry(): StarboardRegistry {
        return StarboardRegistry()
    }

    companion object {
        lateinit var jda: JDA
    }

}