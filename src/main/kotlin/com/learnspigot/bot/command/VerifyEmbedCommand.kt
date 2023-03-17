package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.manager.VerificationManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.interactions.components.danger
import dev.minn.jda.ktx.interactions.components.success
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.ErrorResponse

class VerifyEmbedCommand(
    private val guild: Guild, private val bot: JDA, private val verificationManager: VerificationManager
) {init {
    bot.listener<MessageReceivedEvent> { it ->
        if (it.message.type == MessageType.CHANNEL_PINNED_ADD) {
            if (it.author == bot.selfUser) {
                it.message.delete().queue()
            }
        }
    }
    bot.listener<ButtonInteractionEvent> { event ->
        if (event.componentId == "clickToVerify") {
            val subject: TextInput = TextInput.create("udemyprofileurl", "Udemy Profile URL", TextInputStyle.SHORT)
                .setPlaceholder("Udemy Profile URL").setMinLength(10).setMaxLength(1024).build()

            val modal: Modal =
                Modal.create("verifymodal", "Verify Your Profile").addActionRows(ActionRow.of(subject)).build()

            event.replyModal(modal).queue()
        } else if (event.componentId == "approveuser" || event.componentId == "wronglinkuser" || event.componentId == "courseshiddenuser" || event.componentId == "notowneduser") {
            event.deferEdit().queue()
            event.guild!!.getTextChannelById(System.getenv("SUPPORT_CHANNEL_ID"))!!
                .retrieveMessageById(event.message.id).queue { message ->
                    message.editMessageEmbeds(Embed {
                        title = "Profile Verification"
                        description = "${message.embeds[0].description}\n\n<@${event.user.id}> has taken action: ${event.button.label}"
                        color = 0x2b2d31
                    }).queue()
                    message.editMessageComponents(
                        ActionRow.of(
                            success("approveuser", "Approve").asDisabled(),
                            danger("wronglinkuser", "Wrong Link").asDisabled(),
                            danger("courseshiddenuser", "Courses Hidden").asDisabled(),
                            danger("notowneduser", "Not Owned").asDisabled()
                        )
                    ).queue()
                    message.unpin().queue()
                    val user_id =
                        message.embeds[0].description!!.split(" ").toTypedArray()[3].replace("<@", "").replace(">", "")
                    when (event.componentId) {
                        "approveuser" -> {
                            verificationManager.verifyUser(event.guild!!.getMemberById(event.user.id)!!, "", true)
                        }
                        "wronglinkuser" -> {
                            event.guild!!.getMemberById(user_id)!!.user.openPrivateChannel().queue({ channel ->
                                channel.sendMessageEmbeds(Embed {
                                    title = "**Profile Verification**"
                                    description =
                                        "Staff looked at your profile and found that you have sent the wrong profile link!\n\nPlease try verification again, follow these steps to get your Udemy profile:\n\n1️⃣ Hover over your profile picture in the picture in the top right on Udemy\n" +
                                                "2️⃣ Select \"Public profile\" from the dropdown\n" +
                                                "3️⃣ Copy the link from your browser"
                                    color = 0x2b2d31
                                }).queue()
                            }, ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
                        }
                        "courseshiddenuser" -> {
                            event.guild!!.getMemberById(user_id)!!.user.openPrivateChannel().queue({ channel ->
                                channel.sendMessageEmbeds(Embed {
                                    title = "**Profile Verification**"
                                    description =
                                        "Staff looked at your profile and found that you have got privacy settings disabled which means we can't see your course!\n\nChange here: https://www.udemy.com/instructor/profile/privacy\n\nEnable \"Show courses you're taking on your profile page\" and verify again!"
                                    color = 0x2b2d31
                                }).queue()
                            }, ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
                        }
                        "notowneduser" -> {
                            event.guild!!.getMemberById(user_id)!!.user.openPrivateChannel().queue({ channel ->
                                channel.sendMessageEmbeds(Embed {
                                    title = "**Profile Verification**"
                                    description =
                                        "Staff looked at your profile and found that you do not own the course!\n\nWas this a mistake? Head to <#${
                                            System.getenv(
                                                "QUESTIONS_CHANNEL_ID"
                                            )
                                        }> and solve this with our staff."
                                    color = 0x2b2d31
                                }).queue()
                            }, ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
                        }
                    }
                }
        }
    }
    bot.listener<ModalInteractionEvent> { event ->
        if (event.modalId == "verifymodal") {
            event.replyEmbed({
                title = "Your profile has been received!"
                description =
                    "Please wait a few moments as staff verify that you own the course. Once verified, this channel will disappear and you'll be able to talk in the rest of the server.\n\nIf you have any concerns, please ask in <#${
                        System.getenv("QUESTIONS_CHANNEL_ID")
                    }>"
                color = 0x2b2d31
            }, ephemeral = true).queue()
            val msg = event.guild!!.getTextChannelById(System.getenv("SUPPORT_CHANNEL_ID"))!!.sendMessageEmbeds(Embed {
                title = "Profile Verification"
                description = "Please verify that <@${event.user.id}> owns the course.\n\n${event.values[0].asString}"
                color = 0x2b2d31
            }).addActionRow(
                success("approveuser", "Approve"),
                danger("wronglinkuser", "Wrong Link"),
                danger("courseshiddenuser", "Courses Hidden"),
                danger("notowneduser", "Not Owned")
            ).complete()
            msg.pin().queue()
        }
    }
}

    fun verifyEmbedCommand() {
        guild.upsertCommand("verifyembed", "Send verify embed") {
            restrict(guild = true, Permission.ADMINISTRATOR)
            bot.onCommand("verifyembed") {
                it.deferReply(true).queue()
                it.guild!!.getTextChannelById(System.getenv("VERIFY_CHANNEL_ID"))!!.sendMessageEmbeds(Embed {
                    title = "**VERIFY YOU OWN THE COURSE**"
                    description =
                        "Welcome to the Discord for the <:lscolor:${System.getenv("EMOJI_LSCOLOR_ID")}> LearnSpigot course!\n\n**😔 Don't own the course?** See <#${
                            System.getenv("GETCOURSE_CHANNEL_ID")
                        }>.\n\nThe URL you need to use is the link to your public profile, to get this:\n1️⃣ Hover over your profile picture in the picture in the top right on Udemy\n2️⃣ Select \"Public profile\" from the dropdown\n3️⃣ Copy the link from your browser\n\nPlease make sure that you have the [privacy settings](https://www.udemy.com/instructor/profile/privacy/) enabled so that we can verify you own the course.\n\nOnce you've verified, you'll have access to our **50 man support team, 50+ additional tutorials** and a **supportive community**"
                    color = 0x2b2d31
                }).addActionRow(success("clickToVerify", "Click to Verify")).complete()
            }
        }.queue()
    }

}
