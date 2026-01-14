package com.learnspigot.bot.verification

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.addRole
import com.learnspigot.bot.Server.canVerify
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.Server.isStudent
import com.learnspigot.bot.Server.replyEphemeral
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.set
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.requests.ErrorResponse
import org.bson.Document
import org.litote.kmongo.findOne
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class VerificationListener: ListenerAdapter() {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    private inline val guild get() = Server.GUILD

    /** user idLong -> URL -- Intended use is for supporting undoing verifications without hitting db more than necessary */
    private val urlCache: Cache<Long, String> = CacheBuilder<Long, String>.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).build()

    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        if (e.button.id == null) return

        if (e.button.id.equals("verify")) {

            if (e.member.isStudent) {
                return e.replyEphemeral("You're already a student!")
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

        val info = e.button.id!!.split("|")

        if (e.button.id!!.startsWith("v|")) {

            val action = info[1]
            val userId = info[2]
            val member = guild.getMemberById(userId) ?: return e.replyEphemeral("Unable to determine user attempting to verify (Did they leave?)")

            if (!member.canVerify) {
                return e.replyEphemeral("You are not permitted to verify student profiles.")
            }

            val questionChannel = Server.CHANNEL_QUESTIONS
            var description = ""

            when (action) {
                "a" -> {
                    val url = Mongo.pendingVerificationsCollection.find(Filters.eq("userId", userId)).first()?.get("url")
                        ?: return e.replyEphemeral("Could not find this users verification request in the database, is this a duplicate?")

                    description = "has approved :mention:'s profile"

                    member.addRole(Server.ROLE_STUDENT)

                    Server.CHANNEL_GENERAL.sendMessageEmbeds(
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
                        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                    }, null)

                    profileRegistry.findByUser(member.user).let {
                        it.udemyProfileUrl = url as String
                        it.save()
                    }

                    Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                }

                "wl" -> {
                    description = "hasn't approved :mention:, as they specified an invalid link"

                    questionChannel.sendMessage(member.asMention).setEmbeds(
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

                    // Delete verification request
                    Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                }

                "ch" -> {
                    description = "hasn't approved :mention:, as they're unable to view their courses"

                    questionChannel.sendMessage(member.asMention).setEmbeds(
                        embed()
                            .setTitle("Profile Verification")
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

                    questionChannel.sendMessage(member.asMention).setEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription("Staff looked at your profile and found that you do not own the course. If you have purchased the course, please make sure it's visible on your public profile.")
                            .build()
                    ).queue()

                    // Delete verification request
                    Mongo.pendingVerificationsCollection.deleteOne(Filters.eq("userId", userId))
                }

                "u" -> {
                    val originalActionTaker = info[3]
                    if (e.member!!.id != originalActionTaker && !e.member.isManager) {
                        return e.replyEphemeral("You can't undo this decision.")
                    }

                    val urlApproved = Mongo.userCollection.findOne(Filters.eq("_id", userId))?.getString("udemyProfileUrl")
                    val url = urlApproved
                        ?: urlCache.getIfPresent(userId)
                        ?: return e.replyEphemeral("Unable to undo this decision as their original URl cannot be found.")

                    // The previous decision was "approved"- If not approved, nothing changed so no need to do anything extra.
                    if (urlApproved != null) {
                        guild.removeRoleFromMember(member, Server.ROLE_STUDENT).queue()
                        Mongo.userCollection.updateOne(Filters.eq("_id", userId), set("udemyProfileUrl", null))
                    }

                    e.message.editMessageEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription(
                                "Please verify that " + member.asMention + " owns the course." +
                                        "\n\nPrevious action reverted by: ${e.member!!.asMention}"
                            )
                            .addField("Udemy Link", url, false)
                            .build()
                    )
                        .setActionRow(*getVerificationActionRow(member))
                        .queue()

                    e.interaction.deferEdit().queue()

                    questionChannel.sendMessage(member.asMention).setEmbeds(
                        embed()
                            .setTitle("Profile Verification")
                            .setDescription(
                                "Please disregard the previous message regarding your verification status - a staff member has reverted the action. Please remain patient while waiting for a corrected decision.\n\n" +
                                        "If you were previously verified and granted the Student role, the role has been removed pending the corrected decision from staff."
                            )
                            .build()
                    ).queue()

                    Mongo.pendingVerificationsCollection.insertOne(Document().append("userId", userId).append("url", url))
                    return
                }
            }

            e.message.editMessageEmbeds(
                embed()
                    .setTitle("Profile Verification")
                    .setDescription(e.member!!.asMention + " " + description.replace(":mention:", member.asMention) + ".")
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
            e.replyEphemeral("Invalid profile link.")
            return
        }

        val verifying = e.member!!

        if (verifying.isStudent) {
            return e.replyEphemeral("You're already a Student!")
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
                .setTitle("Your profile has been received!")
                .setDescription(
                    """
                    Please wait a short while as staff verify that you own the course! Once verified, this channel will disappear and you'll be able to talk in the rest of the server.
                    
                    If you have any concerns, please ask in <#${Server.CHANNEL_QUESTIONS}>."""
                )
                .build()
        ).setEphemeral(true).queue()

        val verificationEmbed = embed()
            .setTitle("Profile Verification")
            .setDescription(
                "Verify that " + verifying.asMention + " owns the course." +
                        (if (isPersonalPlan) "\n\nNote: Student claims to be on Udemy Personal or Business Plan." else "")
            )
            .addField("Udemy Link", url, false)
            .build()

        val mentionContent = if (isPersonalPlan) {
            "<@${Server.STEPHEN?.id}>"
        } else {
            "<@&${Server.ROLE_VERIFIER.id}> New verification request."
        }

        Server.CHANNEL_SUPPORT.sendMessage(mentionContent)
            .addEmbeds(verificationEmbed)
            .addActionRow(*getVerificationActionRow(verifying))
            .queue()

        urlCache.put(verifying.idLong, url)

        Mongo.pendingVerificationsCollection.insertOne(
            Document("userId", verifying.id).append("url", url)
        )
    }

    private fun getVerificationActionRow(member: Member): Array<ItemComponent> {
        return arrayOf(
            Button.success("v|a|" + member.id, "Approve"),
            Button.danger("v|wl|" + member.id, "Wrong Link"),
            Button.danger("v|ch|" + member.id, "Courses Hidden"),
            Button.danger("v|no|" + member.id, "Not Owned")
        )
    }
}