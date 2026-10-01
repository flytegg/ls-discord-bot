package com.learnspigot.bot.counting

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.getEmojiReactionCount
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class VoteBanListener : ListenerAdapter() {

    // Ban length for each successive vote ban. Anything past the end of the list is permanent.
    private val banDurations = listOf(Duration.ofDays(1), Duration.ofDays(7), Duration.ofDays(30))

    // Stops a poll from banning twice if votes come in before the embed is edited
    private val handledPolls = ConcurrentHashMap.newKeySet<String>()

    private val executorService = Executors.newSingleThreadScheduledExecutor()

    init {
        executorService.scheduleAtFixedRate({
            try {
                removeExpiredBans()
            } catch (e: Exception) {
                // Never let an exception kill the scheduler, or nobody would ever get unbanned
                e.printStackTrace()
            }
        }, 0L, 1L, TimeUnit.MINUTES)
    }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.channel.id != Server.CHANNEL_COUNTING.id) return
        if (event.user == null) return
        if (event.user!!.isBot) return
        if (event.emoji != Server.EMOJI_UPVOTE) return
        event.retrieveMessage().queue { message ->
            if (!message.author.isBot) return@queue
            if (message.embeds.isEmpty()) return@queue
            if (message.getEmojiReactionCount(Server.EMOJI_UPVOTE) <= Server.VOTE_COUNTING_BAN_AMOUNT) return@queue
            val userId = message.embeds[0].description?.substringAfter("<@")?.substringBefore(">")?.toLongOrNull() ?: return@queue
            if (!handledPolls.add(message.id)) return@queue

            Server.GUILD.retrieveMemberById(userId).queue { member ->
                val profile = Registry.PROFILES.findByUser(member.user)
                val duration = banDurations.getOrNull(profile.countingBans)
                profile.countingBanned(duration?.let { Instant.now().plus(it).epochSecond })
                Server.GUILD.addRoleToMember(member, Server.ROLE_COUNTING_BANNED).queue()

                val length = if (duration == null) "permanently" else "for ${duration.toDays()} day${if (duration.toDays() == 1L) "" else "s"}"
                println("[Counting Ban] '${member.user.name}' (${member.id}) banned $length (ban #${profile.countingBans})")
                message.editMessageEmbeds(
                    embed().setDescription("<@$userId> has been banned from counting $length.")
                        .setFooter("Counting ban #${profile.countingBans}")
                        .build()
                ).queue { message.clearReactions().queue() }
            }
        }
    }

    private fun removeExpiredBans() {
        val now = Instant.now().epochSecond
        Registry.PROFILES.profileCache.values.toList()
            .filter { it.countingBanExpiry != null && it.countingBanExpiry!! <= now }
            .forEach { profile ->
                profile.countingBanExpired()
                Server.GUILD.removeRoleFromMember(UserSnowflake.fromId(profile.id), Server.ROLE_COUNTING_BANNED).queue(
                    { println("[Counting Ban] Ban expired for '${profile.tag}' (${profile.id})") },
                    { println("[Counting Ban] Unable to remove expired ban role from ${profile.id}: ${it.message}") }
                )
            }
    }

}
