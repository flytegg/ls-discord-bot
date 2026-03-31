package com.learnspigot.bot.verification

import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isStudent
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import jdk.internal.joptsimple.internal.Strings.repeat
import org.bson.Document
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor
import java.security.SecureRandom

class FriendInviteCommand {

    private val random = SecureRandom()
    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    @Command("invite-friend")
    @Description("Create a one-time claim code for a friend")
    fun onInviteFriend(actor: SlashCommandActor) {
        val event = actor.commandEvent()
        val member = event.member ?: return

        if (!member.isStudent) {
            return event.reply("Only verified users can create friend invite codes.").setEphemeral(true).queue()
        }

        val code = generateUniqueCode()
        val now = System.currentTimeMillis()
        val expiresAt = now + (7L * 24 * 60 * 60 * 1000)

        val document = Document("_id", code)
            .append("inviterId", member.id)
            .append("status", "active")
            .append("createdAt", now)
            .append("expiresAt", expiresAt)

        Mongo.friendInvitesCollection.insertOne(document)

        event.reply(
            """
            Friend code created: `$code`
            Invite your friend and give them this code.
            It expires in 7 days and can only be used once.
            """.trimIndent()
        ).setEphemeral(true).queue()
    }

    private fun generateUniqueCode(): String {
        repeat(20) {
            val code = buildString(6) {
                repeat(6) { append(alphabet[random.nextInt(alphabet.length)]) }
            }
            val exists = Mongo.friendInvitesCollection.countDocuments(Filters.eq("_id", code)) > 0
            if (!exists) return code
        }

        throw IllegalStateException("Unable to generate a unique claim code")
    }
}
