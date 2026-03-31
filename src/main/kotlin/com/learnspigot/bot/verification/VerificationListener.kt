package com.learnspigot.bot.verification

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.canVerify
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.Server.isStudent
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.replyEphemeral
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.set
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.label.Label
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionType
import net.dv8tion.jda.api.modals.Modal
import net.dv8tion.jda.api.requests.ErrorResponse
import org.bson.Document
import org.litote.kmongo.findOne
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class VerificationListener: ListenerAdapter() {

    private inline val guild get() = Server.GUILD

    /** user idLong -> URL -- Intended use is for supporting undoing verifications without hitting db more than necessary */
    private val urlCache: Cache<Long, String> = CacheBuilder<Long, String>.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).build()
    /** user idLong -> application reason -- used for management View Info after approve/reject edits hide details */
    private val applicationReasonCache: Cache<Long, String> = CacheBuilder<Long, String>.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).build()

    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        if (e.channel.id != Server.CHANNEL_SUPPORT.id && e.channel.id != Server.CHANNEL_VERIFY.id && e.channel.id != Server.CHANNEL_ALERTS.id) return

        if (e.componentId == "verify_course" || e.componentId == "verify") {

            if (e.member.isStudent) {
                return e.replyEphemeral("You're already verified!")
            }

            val verifyModal = Modal.create("verify", "Verify Udemy Profile")
                .addComponents(
                    Label.of("Udemy Profile URL",
                        TextInput.create("url", TextInputStyle.SHORT)
                            .setPlaceholder("https://www.udemy.com/user/example")
                            .setMinLength(10)
                            .setMaxLength(70)
                            .setRequired(true)
                            .build()
                    ),
                    Label.of(
                        "On Personal/Business Subscription?",
                        TextInput.create("personal_plan", TextInputStyle.SHORT)
                            .setPlaceholder("Yes/No - If you purchased the course directly, answer No")
                            .setMinLength(2)
                            .setMaxLength(3)
                            .setRequired(false)
                            .build()
                    )
                ).build()

            e.replyModal(verifyModal).queue()
            return
        }

        if (e.componentId == "verify_form") {
            if (e.member.isStudent) {
                return e.replyEphemeral("You're already verified!")
            }

            val formModal = Modal.create("verify_form", "Community Access Form")
                .addComponents(
                    Label.of(
                        "Why do you want to join the community?",
                        TextInput.create("reason", TextInputStyle.PARAGRAPH)
                            .setPlaceholder("Tell us a bit about what you're looking for and how you'd benefit from this community.")
                            .setMinLength(10)
                            .setMaxLength(1000)
                            .setRequired(true)
                            .build()
                    )
                )
                .build()

            e.replyModal(formModal).queue()
            return
        }

        if (e.componentId == "verify_friend") {
            if (e.member.isStudent) {
                return e.replyEphemeral("You're already verified!")
            }

            val friendCodeModal = Modal.create("verify_friend_code", "Friend Invite Code")
                .addComponents(
                    Label.of(
                        "Enter your 6-character claim code",
                        TextInput.create("claim_code", TextInputStyle.SHORT)
                            .setPlaceholder("ABC123")
                            .setMinLength(6)
                            .setMaxLength(6)
                            .setRequired(true)
                            .build()
                    )
                )
                .build()

            e.replyModal(friendCodeModal).queue()
            return
        }

        if (e.componentId.startsWith("f|")) {
            val info = e.componentId.split("|")
            val action = info[1]
            val userId = info[2]
            val member = guild.getMemberById(userId) ?: return e.replyEphemeral("Unable to determine user for this application (Did they leave?)")

            if (!e.member.canVerify) {
                return e.replyEphemeral("You are not permitted to review applications.")
            }

            when (action) {
                "a", "r" -> {
                    val reason = e.message.embeds.firstOrNull()
                        ?.fields
                        ?.firstOrNull { it.name == "Why they want to join" }
                        ?.value
                        ?: applicationReasonCache.getIfPresent(member.idLong)
                        ?: "No reason provided."
                    applicationReasonCache.put(member.idLong, reason)

                    if (action == "a") {
                        Server.GUILD.addRoleToMember(member, Server.ROLE_STUDENT).queue()
                        Server.GUILD.addRoleToMember(member, Server.ROLE_VERIFIED_FORM).queue()
                        Server.CHANNEL_GENERAL.sendMessage("Welcome ${member.asMention} to the community! (application form)").queue()
                    }

                    val description = if (action == "a") {
                        "has approved :mention:'s application"
                    } else {
                        "has rejected :mention:'s application"
                    }

                    e.message.editMessageEmbeds(
                        embed()
                            .setTitle("Community Application")
                            .setDescription(e.member!!.asMention + " " + description.replace(":mention:", member.asMention) + ".")
                            .build()
                    )
                        .setComponents(
                            ActionRow.of(
                                Button.danger("f|u|" + member.id + "|" + e.member!!.id + "|" + action, "Undo"),
                                Button.secondary("f|i|" + member.id, "View Info")
                            )
                        )
                        .queue()

                    e.interaction.deferEdit().queue()
                    return
                }

                "u" -> {
                    val originalActionTaker = info[3]
                    val previousAction = info.getOrNull(4) ?: "r"

                    if (e.member!!.id != originalActionTaker && !e.member.isManager) {
                        return e.replyEphemeral("You can't undo this decision.")
                    }

                    if (previousAction == "a") {
                        guild.removeRoleFromMember(member, Server.ROLE_STUDENT).queue()
                        guild.removeRoleFromMember(member, Server.ROLE_VERIFIED_FORM).queue()
                    }

                    val reason = e.message.embeds.firstOrNull()
                        ?.fields
                        ?.firstOrNull { it.name == "Why they want to join" }
                        ?.value
                        ?: applicationReasonCache.getIfPresent(member.idLong)
                        ?: "No reason provided."
                    applicationReasonCache.put(member.idLong, reason)

                    e.message.editMessageEmbeds(
                        embed()
                            .setTitle("Community Application")
                            .setDescription(
                                member.asMention + " submitted an application." +
                                        "\n\nPrevious action reverted by: ${e.member!!.asMention}"
                            )
                            .addField("Why they want to join", reason, false)
                            .build()
                    )
                        .setComponents(getFormActionRow(member))
                        .queue()

                    e.interaction.deferEdit().queue()
                    return
                }

                "i" -> {
                    if (!e.member.isManager) {
                        return e.replyEphemeral("Only management can view application details.")
                    }

                    val reason = e.message.embeds.firstOrNull()
                        ?.fields
                        ?.firstOrNull { it.name == "Why they want to join" }
                        ?.value
                        ?: applicationReasonCache.getIfPresent(member.idLong)
                        ?: "No reason provided."

                    e.replyEmbeds(
                        embed()
                            .setTitle("Application Info")
                            .addField("Reason", reason, false)
                            .build()
                    ).setEphemeral(true).queue()
                    return
                }
            }
        }

        val info = e.componentId.split("|")

        if (e.componentId.startsWith("v|")) {

            val action = info[1]
            val userId = info[2]
            val member = guild.getMemberById(userId) ?: return e.replyEphemeral("Unable to determine user attempting to verify (Did they leave?)")

            if (!e.member.canVerify) {
                return e.replyEphemeral("You are not permitted to verify users.")
            }

            member.user.openPrivateChannel().queue { channel ->

                var description = ""

                when (action) {
                    "i" -> {
                        if (!e.member.isManager) {
                            return@queue e.replyEphemeral("Only management can view verification details.")
                        }

                        val profileUrl = e.message.embeds.firstOrNull()
                            ?.fields
                            ?.firstOrNull { it.name == "Udemy Link" }
                            ?.value
                            ?: Mongo.pendingVerificationsCollection.find(Filters.eq("userId", userId)).first()?.getString("url")
                            ?: Mongo.userCollection.findOne(Filters.eq("_id", userId))?.getString("udemyProfileUrl")
                            ?: urlCache.getIfPresent(userId.toLong())
                            ?: "Profile URL not found."

                        e.replyEmbeds(
                            embed()
                                .setTitle("Udemy Verification Info")
                                .addField("Profile", profileUrl, false)
                                .build()
                        ).setEphemeral(true).queue()
                        return@queue
                    }

                    "a" -> {
                        val url = Mongo.pendingVerificationsCollection.find(Filters.eq("userId", userId)).first()?.get("url")
                            ?: return@queue e.replyEphemeral("Could not find this users verification request in the database, is this a duplicate?")

                        description = "has approved :mention:'s profile"

                        Server.GUILD.addRoleToMember(member, Server.ROLE_STUDENT).queue()
                        Server.GUILD.addRoleToMember(member, Server.ROLE_VERIFIED_UDEMY).queue()
                        Server.CHANNEL_GENERAL.sendMessage("Welcome ${member.asMention} to the community! (Udemy course)").queue()

                        member.user.openPrivateChannel().queue({ channel ->
                            channel.sendMessageEmbeds(
                                embed()
                                    .setTitle("Udemy Profile Verification")
                                    .setDescription("Your profile was approved! Go ahead and enjoy our community :heart:")
                                    .setFooter("PS: Want your free 6 months IntelliJ Ultimate key? Run /getkey in the Discord server!")
                                    .build()
                            ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                        }, null)

                        Registry.PROFILES.findByUser(member.user).let {
                            it.udemyProfileUrl = url as String
                            it.save()
                        }

                        Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                    }

                    "wl" -> {
                        description = "hasn't approved :mention:, as they specified an invalid link"

                        channel.sendMessageEmbeds(
                            embed()
                                .setTitle("Udemy Profile Verification")
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

                        // Delete verification request
                        Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                    }

                    "ch" -> {
                        description = "hasn't approved :mention:, as they're unable to view their courses"

                        channel.sendMessageEmbeds(
                            embed()
                                .setTitle("Udemy Profile Verification")
                                .setDescription(
                                    """
                                    Staff looked at your profile and found that you have privacy settings disabled which means we can't see your courses.
                                                                    
                                    Change here: <https://www.udemy.com/instructor/profile/privacy/>
                                                                    
                                    Enable "Show courses you're taking on your profile page" and verify again!
                                    """
                                )
                                .build()
                        ).queue()

                        // Delete verification request
                        Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                    }

                    "no" -> {
                        description = "hasn't approved :mention:, as they do not own the course"

                        channel.sendMessageEmbeds(
                            embed()
                                .setTitle("Udemy Profile Verification")
                                .setDescription("Staff looked at your profile and found that you do not own the course. If you have purchased the course, please make sure it's visible on your public profile.")
                                .build()
                        ).queue()

                        // Delete verification request
                        Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                    }

                    "u" -> {
                        val originalActionTaker = info[3]
                        if (e.member!!.id != originalActionTaker && !e.member.isManager) {
                            return@queue e.replyEphemeral("You can't undo this decision.")
                        }

                        val urlApproved = Mongo.userCollection.findOne(Filters.eq("_id", userId))?.getString("udemyProfileUrl")
                        val url = urlApproved
                            ?: urlCache.getIfPresent(userId.toLong())
                            ?: return@queue e.replyEphemeral("Unable to undo this decision as their original URL cannot be found.")

                        // The previous decision was "approved"- If not approved, nothing changed so no need to do anything extra.
                        if (urlApproved != null) {
                            guild.removeRoleFromMember(member, Server.ROLE_STUDENT).queue()
                            guild.removeRoleFromMember(member, Server.ROLE_VERIFIED_UDEMY).queue()
                            Mongo.userCollection.updateOne(Filters.eq("_id", userId), set("udemyProfileUrl", null))
                        }

                        e.message.editMessageEmbeds(
                            embed()
                                .setTitle("Udemy Profile Verification")
                                .setDescription(
                                    "Please verify that " + member.asMention + " owns the Udemy course." +
                                            "\n\nPrevious action reverted by: ${e.member!!.asMention}"
                                )
                                .addField("Udemy Link", url, false)
                                .build()
                        )
                            .setComponents(getVerificationActionRow(member))
                            .queue()

                        e.interaction.deferEdit().queue()

                        channel.sendMessageEmbeds(
                            embed()
                                .setTitle("Udemy Profile Verification")
                                .setDescription(
                                    "Please disregard the previous message regarding your verification status - a staff member has reverted the action. Please remain patient while waiting for a corrected decision.\n\n" +
                                            "If you were previously verified and granted the Student role, the role has been removed pending the corrected decision from staff."
                                )
                                .build()
                        ).queue()

                        Mongo.pendingVerificationsCollection.insertOne(Document().append("userId", userId).append("url", url))
                        return@queue
                    }
                }

                e.message.editMessageEmbeds(
                    embed()
                        .setTitle("Udemy Profile Verification")
                        .setDescription(e.member!!.asMention + " " + description.replace(":mention:", member.asMention) + ".")
                        .build()
                )
                    .setComponents(
                        ActionRow.of(
                            Button.danger("v|u|" + member.id + "|" + e.member!!.id, "Undo"),
                            Button.secondary("v|i|" + member.id, "View Info")
                        )
                    )
                    .queue()

                e.interaction.deferEdit().queue()

            }
        }
    }

    override fun onModalInteraction(e: ModalInteractionEvent) {
        if (e.interaction.type != InteractionType.MODAL_SUBMIT) return

        if (e.modalId == "verify_friend_code") {
            val member = e.member ?: return
            if (member.isStudent) {
                return e.replyEphemeral("You're already verified!")
            }

            val claimCode = e.getValue("claim_code")?.asString?.trim()?.uppercase() ?: ""
            if (!Regex("^[A-Z0-9]{6}$").matches(claimCode)) {
                return e.replyEphemeral("That claim code is invalid. Please check and try again.")
            }

            val document = Mongo.friendInvitesCollection.find(
                Filters.and(
                    Filters.eq("_id", claimCode),
                    Filters.eq("status", "active"),
                    Filters.exists("usedBy", false)
                )
            ).first() ?: return e.replyEphemeral("That claim code is invalid or has already been used.")

            val expiresAt = (document.get("expiresAt") as? Number)?.toLong() ?: 0L
            val now = System.currentTimeMillis()
            if (expiresAt > 0 && now > expiresAt) {
                Mongo.friendInvitesCollection.updateOne(
                    Filters.eq("_id", claimCode),
                    Document("\$set", Document("status", "expired"))
                )
                return e.replyEphemeral("That claim code has expired. Ask your friend for a new one.")
            }

            val updateResult = Mongo.friendInvitesCollection.updateOne(
                Filters.and(
                    Filters.eq("_id", claimCode),
                    Filters.eq("status", "active"),
                    Filters.exists("usedBy", false)
                ),
                Document("\$set", Document("status", "used")
                    .append("usedBy", member.id)
                    .append("usedAt", now))
            )

            if (updateResult.modifiedCount == 0L) {
                return e.replyEphemeral("That claim code is no longer valid. Ask your friend for a new code.")
            }

            val inviterId = document.getString("inviterId") ?: ""

            Server.GUILD.addRoleToMember(member, Server.ROLE_STUDENT).queue()
            Server.GUILD.addRoleToMember(member, Server.ROLE_VERIFIED_FRIEND_CODE).queue()
            if (inviterId.isNotBlank()) {
                Server.CHANNEL_GENERAL.sendMessage("Welcome ${member.asMention} to the community! (invited by <@$inviterId>)").queue()
            } else {
                Server.CHANNEL_GENERAL.sendMessage("Welcome ${member.asMention} to the community! (friend invite)").queue()
            }

            e.replyEmbeds(
                embed()
                    .setTitle("You have accepted a friend invite!")
                    .setDescription("Welcome to the community! Your Student role has been granted. Go ahead and start chatting in the server!")
                    .build()
            ).setEphemeral(true).queue()
            return
        }

        if (e.modalId == "verify_form") {
            val applicant = e.member ?: return
            val reason = e.getValue("reason")?.asString?.trim().orEmpty()
            applicationReasonCache.put(applicant.idLong, reason.ifBlank { "No reason provided." })

            Server.CHANNEL_ALERTS.sendMessage("<@&${Server.ROLE_VERIFIER.id}> New application")
                .addEmbeds(
                    embed()
                        .setTitle("Community Application")
                        .setDescription("${applicant.asMention} submitted an application.")
                        .addField("Why they want to join", reason.ifBlank { "No reason provided." }, false)
                        .build()
                )
                .addComponents(getFormActionRow(applicant))
                .queue()

            e.replyEmbeds(
                embed()
                    .setTitle("Your application has been received!")
                    .setDescription(
                        """
                        Thanks for applying. Please wait a short while while staff reviews your submission.

                        If you have any concerns, please ask in <#${Server.CHANNEL_QUESTIONS.idLong}>.
                        """.trimIndent()
                    )
                    .build()
            ).setEphemeral(true).queue()
            return
        }

        if (e.modalId != "verify") return

        var url = e.getValue("url")!!.asString
        val isPersonalPlan = e.getValue("personal_plan")?.asString?.lowercase() == "yes"

        if (url.contains("|") || url.startsWith("https://www.udemy.com/course")) {
            e.replyEphemeral("Invalid profile link.")
            return
        }

        val verifying = e.member!!

        if (verifying.isStudent) {
            return e.replyEphemeral("You're already verified!")
        }

        if (url.endsWith("/")) {
            url = url.dropLast(1)
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
                .setTitle("Your Udemy profile has been received!")
                .setDescription(
                    """
                    Please wait a short while as staff verify that you own the course! Once verified, this channel will disappear and you'll be able to talk in the rest of the server.
                    
                    If you have any concerns, please ask in <#${Server.CHANNEL_QUESTIONS.idLong}>."""
                )
                .build()
        ).setEphemeral(true).queue()


        val verificationEmbed = embed()
            .setTitle("Udemy Profile Verification")
            .setDescription(
                "Verify that " + verifying.asMention + " owns the Udemy course." +
                        (if (isPersonalPlan) "\n\nNote: Student claims to be on Udemy Personal or Business Plan." else "")
            )
            .addField("Udemy Link", url, false)
            .build()

        val mentionContent = if (isPersonalPlan) {
            "<@${Server.STEPHEN?.id}>"
        } else {
            "<@&${Server.ROLE_VERIFIER.id}> New Udemy verification"
        }

        Server.CHANNEL_ALERTS.sendMessage(mentionContent)
            .addEmbeds(verificationEmbed)
            .addComponents(getVerificationActionRow(verifying))
            .queue()

        urlCache.put(verifying.idLong, url)

        Mongo.pendingVerificationsCollection.insertOne(
            Document("userId", verifying.id).append("url", url)
        )
    }

    private fun getVerificationActionRow(member: Member): ActionRow {
        return ActionRow.of(
            Button.success("v|a|" + member.id, "Approve"),
            Button.danger("v|wl|" + member.id, "Wrong Link"),
            Button.danger("v|ch|" + member.id, "Courses Hidden"),
            Button.danger("v|no|" + member.id, "Not Owned")
        )
    }

    private fun getFormActionRow(member: Member): ActionRow {
        return ActionRow.of(
            Button.success("f|a|" + member.id, "Approve"),
            Button.danger("f|r|" + member.id, "Reject")
        )
    }
}
