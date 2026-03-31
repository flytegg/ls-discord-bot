package com.learnspigot.bot.verification

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bson.Document

class FriendInviteListener : ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.id != Server.GUILD_ID) return

        val invites = try {
            event.guild.retrieveInvites().complete()
        } catch (_: Exception) {
            emptyList()
        }
        val activeInviteDocs = Mongo.friendInvitesCollection.find(Filters.eq("status", "active")).toList()

        val matchedActive = activeInviteDocs.firstOrNull { document ->
            val code = document.getString("_id") ?: return@firstOrNull false
            val previousUses = document.getInteger("uses", 0)
            val invite = invites.firstOrNull { it.code == code } ?: return@firstOrNull false
            invite.uses > previousUses
        }

        if (matchedActive != null) {
            val code = matchedActive.getString("_id") ?: return
            val invite = invites.firstOrNull { it.code == code } ?: return
            val inviterId = matchedActive.getString("inviterId") ?: return
            markInviteUsed(code, inviterId, event.user.id, invite.uses)

            event.guild.addRoleToMember(event.member, Server.ROLE_STUDENT).queue()
            Server.CHANNEL_GENERAL.sendMessage("Welcome ${event.member.asMention} to the community! (invited by <@$inviterId>)").queue()
            return
        }

        // One-time invites can be deleted before invite-use diff is visible; recover using the most recent pending marker.
        val now = System.currentTimeMillis()
        val matchedFallback = Mongo.friendInvitesCollection.find(
            Filters.and(
                Filters.eq("status", "consumed-awaiting-join"),
                Filters.exists("usedBy", false)
            )
        ).toList()
            .filter { doc ->
                val deletedAt = (doc.get("deletedAt") as? Number)?.toLong() ?: 0L
                // Keep a wider window to account for membership screening / delayed join events.
                now - deletedAt <= 30 * 60 * 1000
            }
            .maxByOrNull { doc ->
                (doc.get("deletedAt") as? Number)?.toLong() ?: 0L
            }

        if (matchedFallback != null) {
            val code = matchedFallback.getString("_id") ?: return
            val inviterId = matchedFallback.getString("inviterId") ?: return
            markInviteUsed(code, inviterId, event.user.id, 1)

            event.guild.addRoleToMember(event.member, Server.ROLE_STUDENT).queue()
            Server.CHANNEL_GENERAL.sendMessage("Welcome ${event.member.asMention} to the community! (invited by <@$inviterId>)").queue()
        }
    }

    override fun onGuildInviteDelete(event: GuildInviteDeleteEvent) {
        if (event.guild.id != Server.GUILD_ID) return

        val existing = Mongo.friendInvitesCollection.find(Filters.eq("_id", event.code)).first() ?: return
        val status = existing.getString("status") ?: "active"

        if (status == "active") {
            Mongo.friendInvitesCollection.updateOne(
                Filters.eq("_id", event.code),
                Document("\$set", Document("status", "consumed-awaiting-join")
                    .append("deletedAt", System.currentTimeMillis()))
            )
            return
        }

        Mongo.friendInvitesCollection.updateOne(
            Filters.eq("_id", event.code),
            Document("\$set", Document("status", "deleted"))
        )
    }

    private fun markInviteUsed(code: String, inviterId: String, usedBy: String, uses: Int) {
        Mongo.friendInvitesCollection.updateOne(
            Filters.eq("_id", code),
            Document("\$set", Document("status", "used")
                .append("uses", uses)
                .append("inviterId", inviterId)
                .append("usedBy", usedBy)
                .append("usedAt", System.currentTimeMillis()))
        )
    }
}
