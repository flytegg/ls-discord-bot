package com.learnspigot.bot

import com.learnspigot.bot.counting.CountingCommand
import com.learnspigot.bot.database.counting.CountingManager
import com.learnspigot.bot.help.PasteCommand
import com.learnspigot.bot.help.search.HelpPostRegistry
import com.learnspigot.bot.intellijkey.IJUltimateKeyRegistry
import com.learnspigot.bot.knowledgebase.KnowledgebasePostRegistry
import com.learnspigot.bot.lecture.LectureRegistry
import com.learnspigot.bot.reputation.LeaderboardMessage
import com.learnspigot.bot.starboard.StarboardRegistry
import com.learnspigot.bot.verification.VerificationMessage
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import gg.flyte.neptune.annotation.Instantiate
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy

object Bot {
    var jda: JDA = light(Environment.BOT_TOKEN) {
        setActivity(Activity.watching("learnspigot.com"))
        setMemberCachePolicy(MemberCachePolicy.ALL)
        setChunkingFilter(ChunkingFilter.ALL)

        intents += listOf(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_INVITES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT
        )
    }

    init {
        jda.awaitReady()

        Server // intentional to initialize vals

        VerificationMessage(Server.guild)
        LeaderboardMessage()

        // Init Commands
        PasteCommand
        CountingCommand


        Server.guild.updateCommands {
            slash("pastebin", "Share the link to the custom pastebin")

            slash("countingstats", "View counting statistics") {
                option<User>("user", "The user to view stats for")
            }
        }.queue()

//        Server.guild.updateCommands().addCommands(
//            Commands.context(Command.Type.MESSAGE, "Set vote")
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.STUDENT)),
//            Commands.context(Command.Type.MESSAGE, "Set Tutorial vote")
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT)),
//            Commands.context(Command.Type.MESSAGE, "Set Project vote")
//                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT))
//        ).complete()

//        Neptune.Builder(jda, this)
//            .addGuilds(Server.guild)
//            .clearCommands(false)
//            .registerAllListeners(true)
//            .create()
    }

}