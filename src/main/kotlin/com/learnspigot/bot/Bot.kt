package com.learnspigot.bot

import com.learnspigot.bot.counting.CountingRegistry
import com.learnspigot.bot.help.search.HelpPostRegistry
import com.learnspigot.bot.intellijkey.IJUltimateKeyRegistry
import com.learnspigot.bot.knowledgebase.KnowledgebasePostRegistry
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.reputation.LeaderboardMessage
import com.learnspigot.bot.starboard.StarboardRegistry
import com.learnspigot.bot.util.PermissionRole
import com.learnspigot.bot.verification.VerificationMessage
import com.learnspigot.bot.videos.udemy.UdemyRegistry
import com.learnspigot.bot.videos.youtube.YouTubeRegistry
import gg.flyte.neptune.Neptune
import gg.flyte.neptune.annotation.Instantiate
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy

object Bot {

    var jda: JDA private set

    init {
        val env = Dotenv.configure().systemProperties().load()

        jda = JDABuilder.createDefault(env.get("BOT_TOKEN"))
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

        val guild = Server.GUILD // It is important that server is initialised here.

        VerificationMessage()
        LeaderboardMessage()

        guild.updateCommands().addCommands(
            Commands.context(Command.Type.MESSAGE, "Set vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.STUDENT)),
            Commands.context(Command.Type.MESSAGE, "Set Tutorial vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT)),
            Commands.context(Command.Type.MESSAGE, "Set Project vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT))
        ).complete()

        Neptune.Builder(jda, this)
            .addGuilds(guild)
            .clearCommands(false)
            .registerAllListeners(true)
            .create()
    }

    @Instantiate
    fun udemyRegistry(): UdemyRegistry {
        return UdemyRegistry()
    }

    @Instantiate
    fun youTubeRegistry(): YouTubeRegistry {
        return YouTubeRegistry()
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
    fun knowledgebasePostRegistry(): KnowledgebasePostRegistry {
        return KnowledgebasePostRegistry()
    }

    @Instantiate
    fun helpPostRegistry(): HelpPostRegistry {
        return HelpPostRegistry()
    }

}