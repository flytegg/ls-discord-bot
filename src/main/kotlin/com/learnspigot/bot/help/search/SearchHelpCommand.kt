package com.learnspigot.bot.help.search

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SearchHelpCommand {

    @Inject
    private lateinit var helpPostRegistry: HelpPostRegistry

    @Command(
        name = "searchhelp",
        description = "Search the Help channel for a post"
    )
    fun onSearchHelpCommand(
        event: SlashCommandInteractionEvent,
        @Description("Post title or keywords") query: String
    ) {
        val posts = helpPostRegistry.findTop4Posts(query)
        if (posts.size == 0) {
            event.reply("No post was found. This is probably an error and should not happen.").setEphemeral(true)
                .queue()
            return
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