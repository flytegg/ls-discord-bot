package com.learnspigot.bot.moderation

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class MrBeastWatcher : ListenerAdapter() {

    private data class MessageRecord(
        val channelId: Long,
        val attachmentCount: Int,
        val attachments: List<Message.Attachment>,
        val messageId: Long,
        val timestamp: Instant = Instant.now()
    )

    private val recentMessages = ConcurrentHashMap<Long, MutableList<MessageRecord>>()

    // userId → expiry instant; pruned lazily on each message event - DO WE NEED THIS?
    private val actioned = ConcurrentHashMap<Long, Instant>()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return
        if (event.author.isBot) return

        val attachments = event.message.attachments
        if (attachments.isEmpty()) return

        val userId = event.author.idLong
        val now = Instant.now()
        // Lazy clean-up
        actioned.entries.removeIf { it.value.isBefore(now) }
        if (actioned.containsKey(userId)) return

        val record = MessageRecord(
            channelId = event.channel.idLong,
            attachmentCount = attachments.size,
            attachments = attachments,
            messageId = event.messageIdLong
        )

        val userRecords = recentMessages.getOrPut(userId) { mutableListOf() }

        val cutoff = Instant.now().minusSeconds(15)
        userRecords.removeIf { it.timestamp.isBefore(cutoff) }
        userRecords.add(record)

        val distinctChannels = userRecords.map { it.channelId }.toSet()
        val suspiciousCount = userRecords.count { it.attachmentCount in 2..4 }

        if (distinctChannels.size >= 3 && suspiciousCount > 3) {
            val snapshot = userRecords.toList()
            userRecords.clear()
            actioned[userId] = now.plus(Duration.ofDays(1))
            handleDetection(event, userId, snapshot)
        }

    }

    private fun handleDetection(event: MessageReceivedEvent, userId: Long, records: List<MessageRecord>) {
        val member = event.guild.getMemberById(userId) ?: return

        // timeout for 3 days
        member.timeoutFor(Duration.ofDays(3)).reason("MrBeast scam auto-detected").queue()

        val allAttachments = records.flatMap { it.attachments }
        val confidence = if (allAttachments.any { it.isSuspiciousFingerprint() }) "High" else "Medium"

        val channelList = records.joinToString("\n") { rec ->
            val ch = event.guild.getTextChannelById(rec.channelId)
            val name = ch?.asMention ?: rec.channelId.toString()
            "$name (${rec.attachmentCount} attachment${if (rec.attachmentCount != 1) "s" else ""})"
        }

        val attachmentDetails = allAttachments.joinToString("\n") { att ->
            buildString {
                append(att.fileName)
                if (att.width > 0 && att.height > 0) append(" [${att.width}×${att.height}]")
            }
        }

        val embed = embed()
            .setTitle("MrBeast Scam Detected")
            .setColor(Color.RED)
            .addField("User", member.asMention, true)
            .addField("Confidence", confidence, true)
            .addField("Channels (${records.size})", channelList, false)
            .setDescription(attachmentDetails)
            .setFooter("User timed out for 3 days")

        // TODO: In future, potentially stream the first attachment image to the attachment of the post in management?

        // Send alert before deleting — Discord proxies the image immediately on embed post,
        // so the thumbnail remains visible even after the source messages are deleted.
        Server.CHANNEL_MANAGER.sendMessageEmbeds(embed.build()).queue {
            records.forEach { rec ->
                println("Deleting Potential Mr Beast Scam from ${member.effectiveName} in ${rec.channelId}")
                event.guild.getTextChannelById(rec.channelId)
                    ?.deleteMessageById(rec.messageId)
                    ?.queue(null) { /* message may already be gone */ }
            }
        }
    }

    private fun Message.Attachment.isSuspiciousFingerprint(): Boolean {
        val nameMatch = fileName.lowercase() == "image.jpg"
        val sizeMatch = width in 800..850 && height in 1050..1150
        return nameMatch || sizeMatch
    }
}
