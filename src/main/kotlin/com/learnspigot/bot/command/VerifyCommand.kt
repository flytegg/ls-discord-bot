package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.manager.VerificationManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.events.onContext
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands

class VerifyCommand(private val guild: Guild, private val bot: JDA, private val verificationManager: VerificationManager) {

    fun selfVerifyCommand() {
        guild.upsertCommand("verify", "Gain access to the server") {
            option<String>("url", "Your udemy profile link", true)
            restrict(guild = true)
            bot.onCommand("verify") {
                val url = it.getOption("url")!!.asString
                it.reply_(embeds = listOf(verifyUser(it.member!!, url)), ephemeral = true).queue()
            }
        }.queue()
    }

    fun adminVerifyCommand() {
        guild.upsertCommand("verifyother", "Specialist command to verify others") {
            restrict(guild = true, perms = DefaultMemberPermissions.DISABLED)
            option<Member>("member", "The member you wish to verify", true)
            option<String>("url", "The user's udemy profile link", true)
            option<Boolean>("force", "Force the verification (false by default)", false)

            bot.onCommand("verifyother") {
                val member = it.getOption("member")?.asMember!!
                val url = it.getOption("url")!!.asString
                val force = it.getOption("force")?.asBoolean ?: false

                it.reply_(embeds = listOf(verifyUser(member, url, force, verifyOther = true)), ephemeral = true).queue()
            }
        }.queue()
    }

    fun verifyUserContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Verify User").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.DISABLED
            }
        ).queue()

        bot.onContext<User>("Verify User") {
            it.replyModal(Modal("verify-${it.target.id}", "Verify ${it.target.name}") {
                short("url", "Udemy Url", true)
            }).queue()
        }
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("verify-")) {
                val target: Member = it.guild!!.getMemberById(it.modalId.split(Regex.fromLiteral("-"))[1])!!
                val url = it.getValue("url")?.asString!!

                it.reply_(embeds = listOf(verifyUser(target, url, verifyOther = true)), ephemeral = true).queue()
            }
        }
    }

    fun forceVerifyContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Verify User (Forced)").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.DISABLED
            }
        ).queue()

        bot.onContext<User>("Verify User (Forced)") {
            it.replyModal(Modal("force-verify-${it.target.id}", "Verify ${it.target.name}") {
                this.short("url", "Udemy Url", true)
            }).queue()
        }
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("force-verify-")) {
                val target: Member = it.guild!!.getMemberById(it.modalId.split(Regex.fromLiteral("-"))[2])!!
                val url = it.getValue("url")?.asString!!

                it.reply_(embeds = listOf(verifyUser(target, url, force = true, verifyOther = true)), ephemeral = true).queue()
            }
        }
    }

    private fun verifyUser(member: Member, url: String, force: Boolean = false, verifyOther: Boolean = false): MessageEmbed {
        return when(verificationManager.verifyUser(member, url, force)) {
            VerificationManager.VerificationResponse.SUCCESS -> {
                Embed {
                    if(verifyOther || force) {
                        title = "Success"
                        description = "${member.effectiveName} has been verified"
                    } else {
                        title = "Congratulations"
                        description = "You have been verified. Take a look round the server.."
                    }
                    color = EMBED_COLOR
                }
            }
            VerificationManager.VerificationResponse.NOT_OWNED -> {
                Embed {
                    if(verifyOther) {
                        title = "We were unable to verify that ${member.effectiveName} own the course"
                    } else {
                        title = "We were unable to verify that you own the course"
                        description = "If you believe that this was an error, please ping a specialist."
                    }
                    color = EMBED_COLOR
                }
            }
            VerificationManager.VerificationResponse.INVALID_URL -> {
                Embed {
                    title = "Uh oh.."
                    description =
                        "`$url` does not seem to be a valid udemy url. Make sure to follow the instructions or ping a specialist."
                    color = EMBED_COLOR
                }
            }
            VerificationManager.VerificationResponse.ALREADY_VERIFIED -> {
                Embed {
                    title = "You are already verified"
                    description = "You can't verify again"
                }
            }
            VerificationManager.VerificationResponse.DUPLICATE_PROFILE -> {
                Embed {
                    title = "This profile already exists"
                    description =
                        "Someone has already verified with this url. If you think this is an error, please ping a specialist."
                    color = EMBED_COLOR
                }
            }
        }
    }
}