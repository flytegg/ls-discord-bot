package com.learnspigot.bot.misc

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SuggestPaperCommand {

    @Command(
            name = "suggestpaper",
            description = "Suggest the user to use PaperMC"
    )

    fun onSuggestPaperCommand(event: SlashCommandInteractionEvent) {
        event.replyEmbeds(
                embed()
                        .setTitle("Use PaperMC")
                        .setDescription("<:right:1051865413216120853> https://papermc.io/downloads/paper")
                        .addField(
                                "What is PaperMC?",
                                "PaperMC is a fork of Spigot which is more optimized, stable, and has a stronger API.",
                                false
                        )
                        .addField(
                                "Why should I use PaperMC?",
                                "PaperMC improves on Spigot's performance, has a expanding plugin API, and has more features which are needed to run a successful server.",
                                true,
                        ).addField(
                                "Still don't believe me?",
                                "PaperMC and its forks is the most common server software used taking up over 63% of the market. Source: https://bstats.org/global/bukkit#minecraftVersion",
                                false
                        )
                        .setFooter("We also recommend checking out: https://droplegacy.support")
                        .build())
                .queue()

    }

}