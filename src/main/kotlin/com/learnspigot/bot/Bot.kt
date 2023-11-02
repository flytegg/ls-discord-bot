package com.learnspigot.bot

import com.learnspigot.bot.help.HelpPostRegistry
import com.learnspigot.bot.intellijkey.IJUltimateKeyRegistry
import com.learnspigot.bot.knowledgebase.KnowledgebaseRegistry
import com.learnspigot.bot.lecture.LectureRegistry
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.reputation.LeaderboardMessage
import com.learnspigot.bot.starboard.StarboardRegistry
import com.learnspigot.bot.util.PermissionRole
import com.learnspigot.bot.verification.VerificationMessage
import gg.flyte.neptune.Neptune
import gg.flyte.neptune.annotation.Instantiate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
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
        LeaderboardMessage(profileRegistry)

        Neptune.Builder(jda, this)
            .addGuilds(guild)
            .clearCommands(false)
            .registerAllListeners(true)
            .create()

        guild.updateCommands().addCommands(
            Commands.context(Command.Type.MESSAGE, "Set vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.STUDENT)),
            Commands.context(Command.Type.MESSAGE, "Set Tutorial vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT)),
            Commands.context(Command.Type.MESSAGE, "Set Project vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT))
        ).queue()
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

    @Instantiate
    fun keyRegistry(): IJUltimateKeyRegistry {
        return IJUltimateKeyRegistry()
    }

    @Instantiate
    fun knowledgebaseRegistry(): KnowledgebaseRegistry {
        return KnowledgebaseRegistry()
    }

    @Instantiate
    fun helpPostRegistry(): HelpPostRegistry {
        return HelpPostRegistry()
    }

//    @Instantiate
//    fun docRegistry(): DocRegistry {
//        return DocRegistry()
//    }

    companion object {
        lateinit var jda: JDA
    }

}