package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.manager.VerificationManager
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.events.onContext
import dev.minn.jda.ktx.events.targetMember
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands

class UnverifyCommand(private val guild: Guild, private val bot: JDA, private val verificationManager: VerificationManager) {

    fun unverifyCommand() {
        guild.upsertCommand("unverify", "Unverify a user") {
            option<Member>("user", "The user you wish to unverify", true)
            restrict(guild = true, Permission.MANAGE_ROLES)

            bot.onCommand("unverify") {
                it.deferReply().queue()
                try {
                    verificationManager.unverifyUser(it.getOption("user")?.asMember!!)
                    it.replyEmbed({
                        title = "Success"
                        description = "They are no longer verified"
                    }).queue()
                }catch (ex: NullPointerException) {
                    it.replyEmbed({
                        title = "Hmmm.. :/"
                        description = "Unable to find their profile. Are they verified?"
                    }, ephemeral = true).queue()
                }
            }
        }
    }

    fun unverifyContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Unverify User").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.DISABLED
            }
        ).queue()

        bot.onContext<User>("Unverify User") {
            it.deferReply().queue()
            try {
                verificationManager.unverifyUser(it.targetMember!!)
                it.replyEmbed({
                    title = "Success"
                    description = "They are no longer verified"
                }, ephemeral = true).queue()
            }catch (ex: NullPointerException) {
                it.replyEmbed({
                    title = "Hmmm.. :/"
                    description = "Unable to find their profile. Are they verified?"
                }, ephemeral = true).queue()
            }
        }
    }
}