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
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands

class VerifyCommand(private val guild: Guild, private val bot: JDA, private val verificationManager: VerificationManager) {

    fun addVerifyEmbedCommand() {
        guild.upsertCommand("addverifyembed", "Post the verify embed in the current channel") {
            restrict(true, Permission.ADMINISTRATOR)
            bot.onCommand("addverifyembed") {
                it.deferReply(true).queue()
                val channel = it.messageChannel
                val getCourseChannel = guild.getTextChannelById(611625329924243519)
                channel.sendMessageEmbeds(Embed {
                    title = ":police_officer: VERIFY YOU OWN THE COURSE"
                    description = """
                        Welcome to the Discord for the :lscolor: LearnSpigot course!
                        
                        😔 **Don't own the course?** See ${getCourseChannel?.asMention ?: "the get-course channel"}.
                        
                        To verify you own the course, click the button below.
                        
                        The URL you need to use is the link to your public profile, to get this:
                        1️⃣ Hover over your profile picture in the top right on Udemy
                        2️⃣ Select "Public profile" from the dropdown
                        3️⃣ Copy the link from your browser
                        
                        Please make sure that you have [privacy settings](https://www.udemy.com/instructor/profile/privacy/) enabled so that we can verify you own the course.

                        Once you've verified, you'll have access to our **50 man support team**, **50+ additional tutorials** and a **supportive community**.
                    """.trimIndent()
                    color = EMBED_COLOR
                }).setActionRow(
                    success("verify", "Verify", Emoji.fromUnicode("U+2705"))
                ).queue()
                it.hook.deleteOriginal().queue()
            }
            bot.listener<ButtonInteractionEvent> {buttonEvent ->
                if(buttonEvent.componentId != "verify") return@listener
                buttonEvent.replyModal("selfverify-modal", "Verify that you own the course") {
                    paragraph("url", "Your udemy link", true, placeholder = "https://www.udemy.com/user/YourName/")
                }.queue()
            }
            bot.listener<ModalInteractionEvent> {modalEvent ->
                if (modalEvent.modalId != "selfverify-modal") return@listener
                modalEvent.deferReply(true).queue()
                val url = modalEvent.getValue("url")!!.asString
                modalEvent.hook.editOriginal(MessageEdit(embeds = listOf(verifyUser(modalEvent.member!!, url)))).queue()
            }
        }.queue()
    }

    fun adminVerifyCommand() {
        guild.upsertCommand("verifyother", "Specialist command to verify others") {
            restrict(guild = true, Permission.MANAGE_ROLES)
            option<Member>("member", "The member you wish to verify", true)
            option<String>("url", "The user's udemy profile link", true)
            option<Boolean>("force", "Force the verification (false by default)", false)

            bot.onCommand("verifyother") {
                it.deferReply(true).queue()
                val member = it.getOption("member")?.asMember!!
                val url = it.getOption("url")!!.asString
                val force = it.getOption("force")?.asBoolean ?: false

                it.hook.editOriginal(MessageEdit(embeds = listOf(verifyUser(member, url, force, verifyOther = true))))
            }
        }.queue()
    }

    fun verifyUserContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Verify User").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES)
            }
        ).queue()

        bot.onContext<User>("Verify User") {
            it.replyModal(Modal("otherverify-${it.target.id}", "Verify ${it.target.name}") {
                short("url", "Udemy Url", true)
            }).queue()
        }
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("otherverify-")) {
                it.deferReply(true).queue()
                val target: Member = it.guild!!.getMemberById(it.modalId.split(Regex.fromLiteral("-"))[1])!!
                val url = it.getValue("url")?.asString!!
                it.hook.editOriginal(MessageEdit(embeds = listOf(verifyUser(target, url, verifyOther = true)))).queue()
            }
        }
    }

    fun forceVerifyContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Verify User (Forced)").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES)
            }
        ).queue()

        bot.onContext<User>("Verify User (Forced)") {
            it.replyModal(Modal("force-verify-${it.target.id}", "Verify ${it.target.name}") {
                this.short("url", "Udemy Url", true)
            }).queue()
        }
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("force-verify-")) {
                it.deferReply(true).queue()
                val target: Member = it.guild!!.getMemberById(it.modalId.split(Regex.fromLiteral("-"))[2])!!
                val url = it.getValue("url")?.asString!!
                it.hook.editOriginal(MessageEdit(embeds = listOf(verifyUser(target, url, force = true)))).queue()
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
                    color = EMBED_COLOR
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