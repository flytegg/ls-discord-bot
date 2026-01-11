package com.learnspigot.bot.reputation

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageHistory
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class LeaderboardMessage(private val profileRegistry: ProfileRegistry) {

    private val medals: List<String> = listOf(":first_place:", ":second_place:", ":third_place:")

    private val executorService = Executors.newSingleThreadScheduledExecutor()

    private val monthlyRewardMessage: Message
    private val lifetimeMessage: Message
    private val monthlyMessage: Message

    init {
        Server.CHANNEL_LEADERBOARD.apply {
            MessageHistory.getHistoryFromBeginning(this).complete().retrievedHistory.apply {
                /*
                 * If all 3 messages aren't there, delete any existing ones and send the new 3
                 * Otherwise, just get them, edit to update, and store for constant updating like normal
                 */
                if (size != 3) {
                    forEach { it.delete().queue() }
                    monthlyRewardMessage = sendMessageEmbeds(buildPrizeEmbed()).complete()
                    lifetimeMessage = sendMessageEmbeds(buildLeaderboard(false)).complete()
                    monthlyMessage = sendMessageEmbeds(buildLeaderboard(true)).complete()
                } else {
                    monthlyRewardMessage = get(2).editMessageEmbeds(buildPrizeEmbed()).complete()
                    lifetimeMessage = get(1).editMessageEmbeds(buildLeaderboard(false)).complete()
                    monthlyMessage = get(0).editMessageEmbeds(buildLeaderboard(true)).complete()
                }
            }
        }

        executorService.scheduleAtFixedRate({
            lifetimeMessage.editMessageEmbeds(buildLeaderboard(false)).queue()
            monthlyMessage.editMessageEmbeds(buildLeaderboard(true)).queue()

            if (isLastMin()){
                Server.managerChannel.sendMessageEmbeds(buildLeaderboard(true)).queue {println("Manager channel leaderboard message sent.")}
            }
        }, 1L, 1L, TimeUnit.MINUTES)
    }

    private fun buildLeaderboard(monthly: Boolean): MessageEmbed {
        val builder = StringBuilder()

        val i = AtomicInteger(1)
        top10(monthly).forEach(Consumer { (id, reputation): ReputationWrapper ->
            builder.append(
                if (i.get() <= medals.size) medals[i.get() - 1] else i.get().toString() + "."
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

    private fun buildPrizeEmbed() : MessageEmbed{
        return embed()
            .setTitle("Current Monthly Rewards")
            .setDescription("The top 3 on the Monthly Leaderboard will earn these rewards:" +
                    "\n\n${medals[0]} - $50 PayPal!" +
                    "\n${medals[1]} - \$20 PayPal!" +
                    "\n${medals[2]} - \$10 PayPal!")
            .setFooter("* To qualify, you must be part of the Support Team. Message a Manager to apply.", "https://cdn.discordapp.com/avatars/928124622564655184/54b6c4735aff20a92a5bc6881fab4d64.webp?size=128")
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
        val lastMinOfCurrentMonth = startOfNextMonth.minusSeconds(60)

        val isLastMin = now.isAfter(lastMinOfCurrentMonth)
        if (isLastMin){ println("This is the last minute of the month!")}

        return isLastMin
    }


    data class ReputationWrapper(val id: String, val reputation: List<Reputation>)

}