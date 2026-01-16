package com.learnspigot.bot

import com.learnspigot.bot.counting.CountingCommand
import com.learnspigot.bot.counting.CountingListener
import com.learnspigot.bot.counting.VoteBanCommand
import com.learnspigot.bot.counting.VoteBanListener
import com.learnspigot.bot.embed.EmbedCommand
import com.learnspigot.bot.help.*
import com.learnspigot.bot.help.search.SearchHelpCommand
import com.learnspigot.bot.intellijkey.GetKeyCommand
import com.learnspigot.bot.intellijkey.KeysLeftCommand
import com.learnspigot.bot.knowledgebase.KnowledgebaseCommand
import com.learnspigot.bot.knowledgebase.KnowledgebaseListener
import com.learnspigot.bot.notice.NoticeCommand
import com.learnspigot.bot.notice.NoticeListener
import com.learnspigot.bot.profile.ProfileCommand
import com.learnspigot.bot.profile.ProfileListener
import com.learnspigot.bot.reputation.LeaderboardMessage
import com.learnspigot.bot.reputation.command.AddReputationCommand
import com.learnspigot.bot.reputation.command.RemoveReputationCommand
import com.learnspigot.bot.reputation.command.ReputationCommand
import com.learnspigot.bot.showcase.ShowcaseListener
import com.learnspigot.bot.starboard.StarboardListener
import com.learnspigot.bot.suggestion.SuggestionListener
import com.learnspigot.bot.util.ForumKeepAlive
import com.learnspigot.bot.util.PermissionRole
import com.learnspigot.bot.verification.VerificationListener
import com.learnspigot.bot.verification.VerificationMessage
import com.learnspigot.bot.videos.udemy.UdemyCommand
import com.learnspigot.bot.voicechat.VCCommand
import com.learnspigot.bot.voicechat.VCListener
import com.learnspigot.bot.vote.VoteListener
import com.learnspigot.bot.workshop.CloseWorkShopListener
import com.learnspigot.bot.workshop.WorkShopListener
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
import revxrsal.commands.jda.JDALamp
import revxrsal.commands.jda.JDAVisitors
import revxrsal.commands.jda.actor.SlashCommandActor
import java.time.Duration
import java.time.Instant

class Bot {

    companion object {
        private lateinit var env: Dotenv

        lateinit var jda: JDA private set

        fun fromEnv(name: String): String = env.get(name) ?: System.getenv(name) ?: "".also { NullPointerException("Unable to find ENV Variable: $name").printStackTrace() }
    }

    init {
        val startTime = Instant.now()

        env = Dotenv.configure().systemProperties().ignoreIfMissing().load()

        jda = JDABuilder.createDefault(fromEnv("BOT_TOKEN"))
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

        println("JDA Connected! Establishing database connection and Initialising Registries...")

        val guild = Server.GUILD // It is important that server is initialised here.
        Registry.WORKSHOP // Initialise both registry and workshop

        println("Loaded Database and Environment!")

        VerificationMessage()
        LeaderboardMessage()

        registerEvents()

        guild.updateCommands().addCommands(
            Commands.context(Command.Type.MESSAGE, "Set vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.STUDENT)),
            Commands.context(Command.Type.MESSAGE, "Set Tutorial vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT)),
            Commands.context(Command.Type.MESSAGE, "Set Project vote").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.EXPERT)),
            Commands.context(Command.Type.MESSAGE, "Help Notice").setDefaultPermissions(DefaultMemberPermissions.enabledFor(PermissionRole.TRIAL_HELPER)),
         ).complete()

        registerCommands()

        println("Ready! [${Duration.between(startTime, Instant.now()).toSeconds()}s]")
    }

    fun registerEvents() {
        jda.addEventListener(
            CountingListener(),
            CloseListener(),
            HastebinListener(),
            ThreadListener(),
            KnowledgebaseListener(),
            ProfileListener(),
            ShowcaseListener(),
            StarboardListener(),
            SuggestionListener(),
            ForumKeepAlive(),
            VerificationListener(),
            VCListener(),
            VoteListener(),
            CloseWorkShopListener(),
            WorkShopListener(),
            NoticeListener(),
            VoteBanListener(),
        )
    }

    fun registerCommands() {
        val lamp = JDALamp.builder<SlashCommandActor>().build()

        lamp.register(
            CountingCommand(),
            EmbedCommand(),
            SearchHelpCommand(),
            CloseCommand(),
            MultiplierCommand(),
            PasteCommand(),
            GetKeyCommand(),
            KeysLeftCommand(),
            KnowledgebaseCommand(),
            NoticeCommand(),
            ProfileCommand(),
            AddReputationCommand(),
            RemoveReputationCommand(),
            ReputationCommand(),
            UdemyCommand(),
            VCCommand(),
            VoteBanCommand(),
        )

        lamp.accept(JDAVisitors.slashCommands(jda))
    }

}