package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.help.CloseCommand
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class KnowledgebaseCommand {

    @Inject
    private lateinit var knowledgebasePostRegistry: KnowledgebasePostRegistry

    @Command(
        name = "knowledgebase",
        description = "Search the Knowledgebase channel for a post"
    )
    fun onKnowledgebaseCommand(
        event: SlashCommandInteractionEvent,
        @Description("Post title or keywords") query: String
    ) {
        val posts = knowledgebasePostRegistry.findTop4Posts(query)
        if (posts.size == 0) {
            event.reply("No post was found. This is probably an error and should not happen.").setEphemeral(true).queue()
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

        CloseCommand.knowledgebasePostsUsed.getOrPut(event.channel.id) { mutableSetOf() }.add(topPost.id)
    }

}