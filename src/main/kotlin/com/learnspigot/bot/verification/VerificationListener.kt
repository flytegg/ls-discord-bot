package com.learnspigot.bot.verification

import com.learnspigot.bot.Bot
import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.Server.isStaff
import com.learnspigot.bot.Server.isStudent
import com.learnspigot.bot.Server.guild
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.set
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
import org.bson.Document
import org.litote.kmongo.findOne
import java.util.regex.Pattern

class VerificationListener : ListenerAdapter() {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        if (e.button.id == null) return

        if (e.button.id.equals("verify")) {
            if (e.member!!.isStudent) {
                e.reply("You're already a student!").setEphemeral(true).queue()
                return
            }

            val verifyModal = Modal.create("verify", "Verify Your Profile")
                .addActionRows(
                    ActionRow.of(
                        TextInput.create("url", "Udemy Profile URL", TextInputStyle.SHORT)
                            .setPlaceholder("https://www.udemy.com/user/example")
                            .setMinLength(10)
                            .setMaxLength(70)
                            .setRequired(true)
                            .build()
                    ),
                    ActionRow.of(
                        TextInput.create("personal_plan", "On Personal/Business Subscription?", TextInputStyle.SHORT)
                            .setPlaceholder("Yes/No - If you purchased the course directly, answer No")
                            .setMinLength(2)
                            .setMaxLength(3)
                            .setRequired(false)
                            .build()
                    )
                )
                .build()

            e.replyModal(verifyModal).queue()
            return
        }

        val info =  e.button.id!!.split("|")

        if (e.button.id!!.startsWith("v|")) {

            val action = info[1]
            val userId = info[2]
            val member = guild.getMemberById(userId)
            if (member == null) {
                e.reply("Sorry, couldn't find the member").setEphemeral(true).queue()
                return
            }

            val allowedRoles = listOf(
                Environment.get("SUPPORT_ROLE_ID"),
                Environment.get("STAFF_ROLE_ID"),
                Environment.get("MANAGEMENT_ROLE_ID"),
                Environment.get("VERIFIER_ROLE_ID")
            )
            val memberRoles = e.member!!.roles.map { it.id }

            if (allowedRoles.none { it in memberRoles }) {
                e.reply("Sorry, you can't verify student profiles.").setEphemeral(true).queue()
                return
            }

            val questionChannel = guild.getTextChannelById(Environment.get("QUESTIONS_CHANNEL_ID"))

            var description = ""

            when (action) {
                "a" -> {
                    val url = Mongo.pendingVerificationsCollection.find(Filters.eq("userId", userId))?.first()?.get("url")
                    if (url == null) {
                        e.reply("Error, couldn't find url in pending verifications collection.").setEphemeral(true).queue()
                        return
                    }

                    description = "has approved :mention:'s profile"

                    guild.addRoleToMember(member, guild.getRoleById(Environment.get("STUDENT_ROLE_ID"))!!).queue()

                    guild.getTextChannelById(Environment.get("GENERAL_CHANNEL_ID"))!!.sendMessageEmbeds(
                        embed()
                            .setTitle("Welcome")
                            .setDescription("Please welcome " + member.asMention + " as a new Student! :heart:").build()
                    ).queue()

                    member.user.openPrivateChannel().queue({ channel ->
                        channel.sendMessageEmbeds(
                            embed()
                                .setTitle("Profile Verification")
                                .setDescription("Your profile was approved! Go ahead and enjoy our community :heart:")
                                .setFooter("PS: Want your free 6 months IntelliJ Ultimate key? Run /getkey in the Discord server!")
                                .build()
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {
                        })
                    }, null)

                    profileRegistry.findByUser(member.user).let {
                        it.udemyProfileUrl = url as String
                        it.save()
                    }

                    Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                }

                "wl" -> {
                    description = "hasn't approved :mention:, as they specified an invalid link"

                    questionChannel!!.sendMessage(member.asMention).setEmbeds(
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
                    ).queue()
                }

                "ch" -> {
                    description = "hasn't approved :mention:, as they're unable to view their courses"

                    questionChannel!!.sendMessage(member.asMention).setEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription("""
                Staff looked at your profile and found that you have privacy settings disabled which means we can't see your courses.
                                                
                Change here: <https://www.udemy.com/instructor/profile/privacy/>
                                                
                Enable "Show courses you're taking on your profile page" and verify again!
                """)
                            .build()
                    ).queue()
                }

                "no" -> {
                    description = "hasn't approved :mention:, as they do not own the course"

                    questionChannel!!.sendMessage(member.asMention).setEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription("Staff looked at your profile and found that you do not own the course. If you have purchased the course, please make sure it's visible on your public profile.")
                            .build()
                    ).queue()
                }

                "u" -> {
                    val url = Mongo.userCollection.findOne(Filters.eq("_id", userId))?.get("udemyProfileUrl")
                    if (url == null) {
                        e.reply("Error, couldn't find url in user collection.").setEphemeral(true).queue()
                        return
                    }
                    val originalActionTaker = info[3]
                    if (e.member!!.id != originalActionTaker && !e.member!!.roles.contains(e.guild!!.getRoleById(Environment.get("MANAGEMENT_ROLE_ID"))!!)) {
                        e.reply("Sorry, you can't undo that verification decision.").setEphemeral(true).queue()
                        return
                    }

                    guild.removeRoleFromMember(member, guild.getRoleById(Environment.get("STUDENT_ROLE_ID"))!!).queue()
                    e.message.editMessageEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription(
                                "Please verify that " + member.asMention + " owns the course." +
                                        "\n\nPrevious action reverted by: ${e.member!!.asMention}"
                            )
                            .addField("Udemy Link", url as String, false)
                            .build()
                    )
                        .setActionRow(
                            Button.success("v|a|"  + member.id, "Approve"),
                            Button.danger("v|wl|" + member.id, "Wrong Link"),
                            Button.danger("v|ch|" + member.id, "Courses Hidden"),
                            Button.danger("v|no|" + member.id, "Not Owned")
                        )
                        .queue()

                    e.interaction.deferEdit().queue()

                    questionChannel!!.sendMessage(member.asMention).setEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription(
                                "Please disregard the previous message regarding your verification status - a staff member has reverted the action. Please remain patient while waiting for a corrected decision.\n\n" +
                                        "If you were previously verified and granted the Student role, the role has been removed pending the corrected decision from staff."
                            )
                            .build()
                    ).queue()

                    Mongo.userCollection.updateOne(Filters.eq("_id", userId), set("udemyProfileUrl", null))
                    Mongo.pendingVerificationsCollection.insertOne(Document().append("userId", userId).append("url", url))
                    return
                }
            }

            e.message.editMessageEmbeds(
                embed()
                    .setTitle("Profile Verification")
                    .setDescription(
                        e.member!!.asMention + " " + description.replace(
                            ":mention:",
                            member.asMention
                        ) + "."
                    )
                    .build()
            )
                .setActionRow(
                    Button.danger("v|u|" + member.id + "|" + e.member!!.id, "Undo")
                )
                .queue()

            e.interaction.deferEdit().queue()
        }
    }

    override fun onModalInteraction(e: ModalInteractionEvent) {
        if (e.interaction.type != InteractionType.MODAL_SUBMIT) return
        if (e.modalId != "verify") return

        var url = e.getValue("url")!!.asString
        val isPersonalPlan = e.getValue("personal_plan")?.asString?.lowercase() == "yes"

        if (url.contains("|") || url.startsWith("https://www.udemy.com/course")) {
            e.reply("Invalid profile link.").setEphemeral(true).queue()
            return
        }

        if (e.member!!.isStudent) {
            e.reply("You're already a Student!").setEphemeral(true).queue()
            return
        }

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
                    
                    If you have any concerns, please ask in <#${Environment.get("QUESTIONS_CHANNEL_ID")}>."""
                )
                .build()
        ).setEphemeral(true).queue()

        val supportChannel = e.jda.getTextChannelById(Environment.get("SUPPORT_CHANNEL_ID"))!!
        val verificationEmbed = embed()
            .setTitle("Profile Verification")
            .setDescription("Verify that " + e.member!!.asMention + " owns the course." +
                    (if (isPersonalPlan) "\n\nNote: Student claims to be on Udemy Personal or Business Plan." else ""))
            .addField("Udemy Link", url, false)
            .build()

        val mentionContent = if (isPersonalPlan) {
            "<@${Environment.get("STEPHEN_USER_ID")}>"
        } else {
            "<@&${Environment.get("VERIFIER_ROLE_ID")}> New verification request."
        }

        supportChannel.sendMessage(mentionContent)
            .addEmbeds(verificationEmbed)
            .addActionRow(
                Button.success("v|a|" + e.member!!.id, "Approve"),
                Button.danger("v|wl|" + e.member!!.id, "Wrong Link"),
                Button.danger("v|ch|" + e.member!!.id, "Courses Hidden"),
                Button.danger("v|no|" + e.member!!.id, "Not Owned")
            ).queue()

        Mongo.pendingVerificationsCollection.insertOne(
            Document("userId", e.member!!.id)
                .append("url", url)
        )
    }
}