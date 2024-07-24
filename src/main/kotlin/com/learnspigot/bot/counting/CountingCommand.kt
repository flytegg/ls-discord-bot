package com.learnspigot.bot.counting

import com.learnspigot.bot.Bot.jda
import com.learnspigot.bot.database.counting.CountingManager
import com.learnspigot.bot.database.profile.getProfile
import com.learnspigot.bot.util.InvisibleEmbed
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.components.getOption
import net.dv8tion.jda.api.entities.User

object CountingCommand {

    init {
        jda.onCommand("countingstats") { event ->
            val user = event.getOption<User>("user")

            if (user == null) { // Server Stats
                event.replyEmbeds(InvisibleEmbed {
                    title = "Server counting statistics"
                    description = """
                        - Last Count: ${CountingManager.currentCount}
                        - Total Counts: ${CountingManager.serverTotalCounts}
                        - Highest Count: ${CountingManager.topServerCount}
                    """.trimIndent()

                    field {
                        name = "Top 5 counters"
                        value = CountingManager.getTop5().joinToString("") { profile ->
                            "\n- <@${profile.id}>: ${profile.totalCounts}"
                        }
                    }

                }).setEphemeral(true).queue()
            } else {
                val profile = user.getProfile()

                event.replyEmbeds(InvisibleEmbed {
                    title = "${user.name}'s counting statistics"
                    description = """
                        - Total Counts: ${profile?.totalCounts}
                        - Highest Count: ${profile?.highestCount}
                        - Mistakes: ${profile?.countingFuckUps}
                    """.trimIndent()
                })
            }
        }
    }
}