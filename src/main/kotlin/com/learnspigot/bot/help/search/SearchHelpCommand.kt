package com.learnspigot.bot.help.search

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.replyEphemeral
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor

class SearchHelpCommand {

    @Command("searchhelp")
    @Description("Search the Help channel for a post")
    fun onSearchHelpCommand(
        actor: SlashCommandActor,
        @Description("Post title or keywords") query: String
    ) {
        val event = actor.commandEvent()
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