package com.learnspigot.bot.counting

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CountingCommand {

    inline val countingRegistry get() = Registry.COUNTING

    @Command(name = "countingstats", description = "View counting statistics")
    fun onCountingCommand(
        event: SlashCommandInteractionEvent,
        @Description("User's stats to view") @Optional user: User?
    ) {
        if (user == null) { // Server Stats
            event.replyEmbeds(
                embed()
                    .setTitle("Server counting statistics")
                    .setDescription("""
                        - Last Count: ${countingRegistry.currentCount}
                        - Total Counts: ${countingRegistry.serverTotalCounts}
                        - Highest Count: ${countingRegistry.topServerCount}
                    """.trimIndent())
                    .addField(
                        "Top 5 counters",
                        countingRegistry.getTop5().joinToString("") { profile ->
                            "\n- <@${profile.id}>: ${profile.totalCounts}"
                        },
                        false
                    )
                    .build()
            ).setEphemeral(true).queue()
        } else { // Individual Stats
            val profile = Registry.PROFILES.findByUser(user)
            event.replyEmbeds(
                embed()
                    .setTitle(user.name + "'s counting statistics")
                    .setDescription("""
                        - Total Counts: ${profile.totalCounts}
                        - Highest Count: ${profile.highestCount}
                        - Mistakes: ${profile.countingFuckUps}
                    """.trimIndent())
                    .build()
            ).setEphemeral(true).queue()
        }

    }
}