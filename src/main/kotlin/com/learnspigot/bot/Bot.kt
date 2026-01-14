package com.learnspigot.bot

import com.learnspigot.bot.reputation.LeaderboardMessage
import com.learnspigot.bot.util.PermissionRole
import com.learnspigot.bot.verification.VerificationMessage
import gg.flyte.neptune.Neptune
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

class Bot {

    companion object {
        lateinit var jda: JDA private set
    }

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

        workShopPostRegistry.getInfo()

        Neptune.Builder(jda, this)
            .addGuilds(guild)
            .clearCommands(false)
            .registerAllListeners(true)
            .create()

    }

}