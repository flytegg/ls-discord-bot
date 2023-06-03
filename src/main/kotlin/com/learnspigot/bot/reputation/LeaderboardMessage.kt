package com.learnspigot.bot.reputation

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class LeaderboardMessage(guild: Guild, private val profileRegistry: ProfileRegistry) {

    private val MEDALS: List<String> = listOf(":first_place:", ":second_place:", ":third_place:")

    private val EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor()

    private val channel = guild.getTextChannelById(System.getenv("LEADERBOARD_CHANNEL_ID"))!!
    private val managerChannel = guild.getTextChannelById(System.getenv("MANAGER_CHANNEL_ID"))!!

    private var lifetimeMessage = channel.sendMessageEmbeds(buildLeaderboard(false)).complete()
    private var monthlyMessage = channel.sendMessageEmbeds(buildLeaderboard(true)).complete()

    init {
        EXECUTOR_SERVICE.scheduleAtFixedRate({
            lifetimeMessage.editMessageEmbeds(buildLeaderboard(false)).queue()
            monthlyMessage.editMessageEmbeds(buildLeaderboard(true)).queue()

            if (isLastMin()){
                managerChannel.sendMessageEmbeds(buildLeaderboard(true)).queue()
            }
        }, 1L, 1L, TimeUnit.MINUTES)
    }

    private fun buildLeaderboard(monthly: Boolean): MessageEmbed {
        val builder = StringBuilder()

        val i = AtomicInteger(1)
        top10(monthly).forEach(Consumer { (id, reputation): ReputationWrapper ->
            builder.append(
                if (i.get() <= MEDALS.size) MEDALS[i.get() - 1] else i.get().toString() + "."
            ).append(" <@").append(id).append("> - ").append(reputation.size).append("\n")
            i.getAndIncrement()
        })

        return embed()
            .setTitle((if (monthly) "Monthly" else "All-Time") + " Leaderboard")
            .setDescription((if (monthly) "These stats are reset on the 1st of every month." else "These stats are never reset.") + "\n\n$builder")
            .setFooter("Last updated")
            .setTimestamp(Instant.now())
            .build()
    }

    private fun top10(monthly: Boolean): List<ReputationWrapper> {
        val reputation = mutableListOf<ReputationWrapper>()
        for ((key, profile) in profileRegistry.profileCache) {
            var repList = ArrayList(profile.reputation.values)
            if (repList.isEmpty()) continue

            if (monthly) {
                repList = repList.filter { rep ->
                    YearMonth.now().atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                        .isBefore(Instant.ofEpochSecond(rep.timestamp))
                } as ArrayList<Reputation>
            }
            reputation.add(ReputationWrapper(key, repList))
        }
        reputation.sortByDescending { it.reputation.size }
        return reputation.take(10)
    }

    private fun isLastMin(): Boolean {
        val now = Instant.now()
        val startOfNextMonth = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        return now.plusSeconds(61).isAfter(startOfNextMonth)
    }

    data class ReputationWrapper(val id: String, val reputation: List<Reputation>)

}