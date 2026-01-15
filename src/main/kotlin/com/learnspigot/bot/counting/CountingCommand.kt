package com.learnspigot.bot.counting

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.User
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor

class CountingCommand {

    inline val countingRegistry get() = Registry.COUNTING

    @Command("countingstats")
    @Description("View counting statistics")
    fun onCountingCommand(
        actor: SlashCommandActor,
        @Description("User's stats to view") @Optional user: User?
    ) {
        val event = actor.commandEvent()
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