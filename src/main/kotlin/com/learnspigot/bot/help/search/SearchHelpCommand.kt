package com.learnspigot.bot.help.search

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.replyEphemeral
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SearchHelpCommand {

    @Command(
        name = "searchhelp",
        description = "Search the Help channel for a post"
    )
    fun onSearchHelpCommand(
        event: SlashCommandInteractionEvent,
        @Description("Post title or keywords") query: String
    ) {
        val posts = Registry.HELP.findTop4Posts(query)
        if (posts.isEmpty()) {
            return event.replyEphemeral("No post was found. This is probably an error and should not happen.")
        }

        val topPost = posts.removeFirst()
        val suggestions = StringBuilder()
        posts.forEach { post: ThreadChannel ->
            suggestions.append("- ").append(post.asMention).append("\n")
        }

        event.replyEmbeds(
            embed()
                .setTitle(topPost.name)
                .setDescription(topPost.asMention)
                .addField("Not quite what you're looking for?", suggestions.toString(), false)
                .build()
        ).queue()
    }

}