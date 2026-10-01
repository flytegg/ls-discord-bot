package com.learnspigot.bot.help.stats

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.MessageEmbed
import java.time.Instant

fun buildHelpStatsEmbed(): MessageEmbed {
    val stats = Registry.HELP_STATS.stats()

    fun posts(amount: Int) = "$amount post${if (amount == 1) "" else "s"}"
    fun oneDecimal(value: Double) = if (value.isNaN()) "0" else "%.1f".format(value)
    fun duration(seconds: Double) = when {
        seconds.isNaN() -> "N/A"
        seconds < 3600 -> "${oneDecimal(seconds / 60)} minutes"
        seconds < 48 * 3600 -> "${oneDecimal(seconds / 3600)} hours"
        else -> "${oneDecimal(seconds / 86400)} days"
    }

    return embed()
        .setTitle("Help Post Statistics")
        .addField("Posts Open", posts(stats.open), true)
        .addField("Avg. Response Time", duration(stats.avgResponseSeconds), true)
        .addField("Avg. Resolution Time", duration(stats.avgResolutionSeconds), true)
        .addField("Posts Closed (7d)", posts(stats.closedWeek), true)
        .addField("Posts Closed (30d)", posts(stats.closedMonth), true)
        .addField("Posts Closed (Total)", posts(stats.closedTotal), true)
        .addField("Avg. Messages/post (7d)", "${oneDecimal(stats.avgMessagesWeek)} messages", true)
        .addField("Avg. Messages/post (Total)", "${oneDecimal(stats.avgMessagesTotal)} messages", true)
        .addField("Avg. Contributors/post", "${oneDecimal(stats.avgContributors)} contributors", true)
        .addField("Highest Contributor (7d)", stats.topContributorWeek?.let { "<@$it>" } ?: "Nobody yet", true)
        .addField("Most Messages", stats.mostMessages?.let { "${it.messageCount} messages - <#${it.id}>" } ?: "N/A", true)
        .addField("Most Prolific Poster", stats.mostPosts?.let { (id, amount) -> "<@$id> - ${posts(amount)}" } ?: "N/A", true)
        .setFooter(if (Registry.HELP_STATS.syncing) "Still loading past posts... • Last updated" else "Last updated")
        .setTimestamp(Instant.now())
        .build()
}
