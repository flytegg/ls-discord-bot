package com.learnspigot.bot.verification

import com.learnspigot.bot.Environment
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.ErrorResponse
import java.util.regex.Pattern

class VerificationListener : ListenerAdapter() {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        if (e.button.id == null) return

        if (e.button.id.equals("verify")) {
            if (e.member!!.roles.contains(e.jda.getRoleById(Environment.get("STUDENT_ROLE_ID")))) {
                e.reply("You're already a Student!").setEphemeral(true).queue()
                return
            }

            e.replyModal(
                Modal.create("verify", "Verify Your Profile")
                    .addActionRows(
                        ActionRow.of(
                            TextInput.create("url", "Udemy Profile URL", TextInputStyle.SHORT)
                                .setPlaceholder("https://www.udemy.com/user/example")
                                .setMinLength(10)
                                .setRequired(true)
                                .build()
                        )
                    )
                    .build()).queue()
            return
        }

        if (e.button.id!!.startsWith("v|")) {
            val guild = e.guild!!

            val roleIds = e.member!!.roles.map { it.id }
            if (!roleIds.contains(Environment.get("SUPPORT_ROLE_ID")) && !roleIds.contains(Environment.get("STAFF_ROLE_ID")) && !roleIds.contains(Environment.get("MANAGEMENT_ROLE_ID"))) {
                e.reply("Sorry, you can't verify student profiles.").setEphemeral(true).queue()
                return
            }

            val info = e.button.id!!.split("|")
            val action = info[1]
            val url = info[2]
            val member = guild.getMemberById(info[3]) ?: return

            var description = ""

            when (action) {
                "a" -> {
                    description = "has approved :mention:'s profile"

                    guild.addRoleToMember(member, guild.getRoleById(Environment.get("STUDENT_ROLE_ID"))!!).queue()

                    guild.getTextChannelById(Environment.get("GENERAL_CHANNEL_ID"))!!.sendMessageEmbeds(
                        embed()
                            .setTitle("Welcome")
                            .setDescription("Please welcome " + member.asMention + " as a new Student! :heart:").build()).queue()

                    member.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription("Your profile was approved! Go ahead and enjoy our community :heart:")
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }
                "wl" -> {
                    description = "hasn't approved :mention:, as they specified an invalid link"

                    member.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription("""
                                            Staff looked at your profile and found that you have sent the wrong profile link!
                                                                            
                                            The URL you need to use is the link to your public profile, to get this:
                                            :one: Hover over your profile picture in the top right on Udemy
                                            :two: Select "Public profile" from the dropdown menu
                                            :three: Copy the link from your browser
                                            """)
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }
                "ch" -> {
                    description = "hasn't approved :mention:, as they're unable to view their courses"

                    member.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription("""
                                            Staff looked at your profile and found that you have got privacy settings disabled which means we can't see your courses.
                                                                            
                                            Change here: [https://www.udemy.com/instructor/profile/privacy/](https://www.udemy.com/instructor/profile/privacy/)
                                                                            
                                            Enable "Show courses you're taking on your profile page" and verify again!
                                            """)
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }
                "no" -> {
                    description = "hasn't approved :mention:, as they do not own the course"

                    member.user.openPrivateChannel().complete().let {
                        it.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription("Staff looked at your profile and found that you do not own the course!\n\nWas this a mistake? Head to " + guild.getTextChannelById(Environment.get("QUESTIONS_CHANNEL_ID"))!!.asMention + " and solve this with staff.")
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }
                }
            }

            profileRegistry.findByUser(member.user).let {
                it.udemyProfileUrl = url
                it.save()
            }

            e.message.editMessageEmbeds(
                embed()
                    .setTitle("Profile Verification")
                    .setDescription(e.member!!.asMention + " " + description.replace(":mention:", member.asMention) + ".")
                    .build())
                .setActionRow(
                    Button.success("ignore1", "Approve").asDisabled(),
                    Button.danger("ignore2", "Wrong Link").asDisabled(),
                    Button.danger("ignore3", "Courses Hidden").asDisabled(),
                    Button.danger("ignore4", "Not Owned").asDisabled())
                .queue()

            e.interaction.deferEdit().queue()
        }
    }

    override fun onModalInteraction(e: ModalInteractionEvent) {
        if (e.interaction.type != InteractionType.MODAL_SUBMIT) return
        if (e.modalId != "verify") return

        var url = e.getValue("url")!!.asString

        if (url.contains("|")) {
            e.reply("Invalid profile link.").setEphemeral(true).queue()
            return
        }

        if (e.member!!.roles.contains(e.jda.getRoleById(Environment.get("STUDENT_ROLE_ID")))) {
            e.reply("You're already a Student!").setEphemeral(true).queue()
            return
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length - 1);
        }

        if (Mongo.userCollection.countDocuments(Filters.eq("udemyProfileUrl", Pattern.compile(url, Pattern.CASE_INSENSITIVE))) > 0) {
            e.reply("Somebody has already verified with this profile. Was this not you? Let staff know.").setEphemeral(true).queue()
            return
        }

        e.replyEmbeds(
            embed()
                .setTitle("Your profile has been received!")
                .setDescription("""
                        Please wait a few moments as staff verify that you own the course. Once verified, this channel will disappear and you'll be able to talk in the rest of the server.
                        
                        If you have any concerns, please ask in <#${Environment.get("QUESTIONS_CHANNEL_ID")}""" + ">." + """
 
                        """)
                .build()).setEphemeral(true).queue()

        e.jda.getTextChannelById(Environment.get("SUPPORT_CHANNEL_ID"))!!.sendMessageEmbeds(
            embed()
                .setTitle("Profile Verification")
                .setDescription("Please verify that " + e.member!!.asMention + " owns the course.")
                .addField("Udemy Link", url, false)
                .build())
            .addActionRow(
                Button.success("v|a|" + url + "|" + e.member!!.id, "Approve"),
                Button.danger("v|wl|" + url + "|" + e.member!!.id, "Wrong Link"),
                Button.danger("v|ch|" + url + "|" + e.member!!.id, "Courses Hidden"),
                Button.danger("v|no|" + url + "|" + e.member!!.id, "Not Owned")
            ).queue()
    }

}