package com.learnspigot.bot.verification

import com.learnspigot.bot.Server
import com.learnspigot.bot.database.Mongo
import com.learnspigot.bot.database.profile.ProfileManager
import com.learnspigot.bot.database.profile.giveStudentRole
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.isManager
import com.learnspigot.bot.util.isStudent
import com.learnspigot.bot.util.isSupport
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.regex.Pattern

class VerificationListener : ListenerAdapter() {

//    init { // TODO: Register the actual methods via KTX
//        Bot.jda.onButton("verify") {
//            it.reply_("Hello :)").queue()
//        }
//    }

    private val Member.canVerify: Boolean get() = isSupport || roles.contains(Server.verifierRole)

    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        val buttonId = e.button.id ?: return
        val clicker = e.member ?: return

        if (buttonId == "verify") {
            if (clicker.isStudent) return e.reply("You're already a Student!").setEphemeral(true).queue()

            e.replyModal(
                Modal.create("verify", "Verify Your Profile")
                    .addActionRow(
                        TextInput.create("url", "Udemy Profile URL", TextInputStyle.SHORT)
                            .setPlaceholder("https://www.udemy.com/user/example")
                            .setMinLength(10)
                            .setMaxLength(70)
                            .setRequired(true)
                            .build()
                    )
                    .build()
            ).queue()
            return
        }

        if (buttonId.startsWith("v|")) {
            val guild = e.guild!!

            if (!clicker.canVerify)
                return e.reply("Sorry, you can't verify student profiles.").setEphemeral(true).queue()

            val info = e.button.id!!.split("|")
            val action = info[1]
            val url = info[2]
            val targetMember = guild.getMemberById(info[3]) ?: return

            var description = ""

            when (action) {
                "a" -> {
                    description = "has approved :mention:'s profile"
                    targetMember.giveStudentRole()

                    Server.generalChannel.sendMessageEmbeds(
                        embed()
                            .setTitle("Welcome")
                            .setDescription("Please welcome " + targetMember.asMention + " as a new Student! :heart:").build()
                    ).queue()

                    targetMember.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription("Your profile was approved! Go ahead and enjoy our community :heart:")
                                .setFooter("PS: Want your free 6 months IntelliJ Ultimate key? Run /getkey in the Discord server!")
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }

                    ProfileManager.getProfile(targetMember.id)!!.let { // Intentionally throw NPE if error
                        it.udemyProfileUrl = url
                        it.save()
                    }
                }

                "wl" -> {
                    description = "hasn't approved :mention:, as they specified an invalid link"

                    targetMember.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription(
                                    """
                                            Staff looked at your profile and found that you have sent the wrong profile link!
                                                                            
                                            The URL you need to use is the link to your public profile, to get this:
                                            :one: Hover over your profile picture in the top right on Udemy
                                            :two: Select "Public profile" from the dropdown menu
                                            :three: Copy the link from your browser
                                            """
                                )
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }

                "ch" -> {
                    description = "hasn't approved :mention:, as they're unable to view their courses"

                    targetMember.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription(
                                    """
                                            Staff looked at your profile and found that you have got privacy settings disabled which means we can't see your courses.
                                                                            
                                            Change here: <https://www.udemy.com/instructor/profile/privacy/>
                                                                            
                                            Enable "Show courses you're taking on your profile page" and verify again!
                                            """
                                )
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }

                "no" -> {
                    description = "hasn't approved :mention:, as they do not own the course"

                    targetMember.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription(
                                      "Staff looked at your profile and found that you do not own the course! "
                                    + "Are you on the Udemy Personal Plan or Udemy For Business? If so, head to "
                                    + Server.questionsChannel.asMention + " and let staff know."
                                )
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }

                "u" -> {
                    val originalActionTaker = info[4]

                    if (clicker.id != originalActionTaker && !clicker.isManager) {
                        return e.reply("Sorry, you can't undo that verification decision.").setEphemeral(true).queue()
                    }

                    guild.removeRoleFromMember(targetMember, Server.studentRole).queue()
                    e.message.editMessageEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription(
                                "Please verify that " + targetMember.asMention + " owns the course." +
                                        "\n\nPrevious action reverted by: ${clicker.asMention}"
                            )
                            .addField("Udemy Link", url, false)
                            .build()
                    )
                        .setActionRow(
                            Button.success("v|a|" + url + "|" + targetMember.id, "Approve"),
                            Button.danger("v|wl|" + url + "|" + targetMember.id, "Wrong Link"),
                            Button.danger("v|ch|" + url + "|" + targetMember.id, "Courses Hidden"),
                            Button.danger("v|no|" + url + "|" + targetMember.id, "Not Owned")
                        )
                        .queue()

                    e.interaction.deferEdit().queue()

                    targetMember.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription(
                                    "Please disregard the previous message regarding your verification status - a staff member has reverted the action. Please remain patient while waiting for a corrected decision.\n\n" +
                                            "If you were previously verified and granted the Student role, the role has been removed pending the corrected decision from staff."
                                )
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                    return
                }
            }

            e.message.editMessageEmbeds(
                embed()
                    .setTitle("Profile Verification")
                    .setDescription(
                        e.member!!.asMention + " " + description.replace(
                            ":mention:",
                            targetMember.asMention
                        ) + "."
                    )
                    .build()
            )
                .setActionRow(
                    Button.danger("v|u|" + url + "|" + targetMember.id + "|" + e.member!!.id, "Undo")
                )
                .queue()

            e.interaction.deferEdit().queue()
        }
    }

    override fun onModalInteraction(e: ModalInteractionEvent) {
        if (e.interaction.type != InteractionType.MODAL_SUBMIT) return
        if (e.modalId != "verify") return
        val member = e.member ?: return

        var url = e.getValue("url")!!.asString

        if (url.contains("|")) {
            e.reply("Invalid profile link.").setEphemeral(true).queue()
            return
        }

        if (member.isStudent) return e.reply("You're already a Student!").setEphemeral(true).queue()

        if (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }

        if (Mongo.userCollection.countDocuments(
                Filters.eq(
                    "udemyProfileUrl",
                    Pattern.compile(url, Pattern.CASE_INSENSITIVE)
                )
            ) > 0
        ) {
            e.reply("Somebody has already verified with this profile. Was this not you? Let staff know.")
                .setEphemeral(true).queue()
            return
        }

        e.replyEmbeds(
            embed()
                .setTitle("Your profile has been received!")
                .setDescription(
                    """
                        Please wait a short while as staff verify that you own the course! Once verified, this channel will disappear and you'll be able to talk in the rest of the server.
                        
                        If you have any concerns, please ask in ${Server.questionsChannel.asMention}.
 
                        """
                )
                .build()
        ).setEphemeral(true).queue()


        Server.supportChannel.apply {
            sendMessage(Server.verifierRole.asMention).queue { msg -> msg.delete().queue() }
            sendMessageEmbeds(
                embed()
                    .setTitle("Profile Verification")
                    .setDescription("Verify that " + e.member!!.asMention + " owns the course.")
                    .addField("Udemy Link", url, false)
                    .build()
            )
                .addActionRow(
                    Button.success("v|a|" + url + "|" + e.member!!.id, "Approve"),
                    Button.danger("v|wl|" + url + "|" + e.member!!.id, "Wrong Link"),
                    Button.danger("v|ch|" + url + "|" + e.member!!.id, "Courses Hidden"),
                    Button.danger("v|no|" + url + "|" + e.member!!.id, "Not Owned")
                ).queue()
        }
    }

}