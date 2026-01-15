package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Registry
import com.learnspigot.bot.help.CloseCommand
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor

class KnowledgebaseCommand {

    @Command("knowledgebase")
    @Description("Search the Knowledgebase channel for a post")
    fun onKnowledgebaseCommand(
        actor: SlashCommandActor,
        @Description("Post title or keywords") query: String
    ) {
        val event = actor.commandEvent()
        val posts = Registry.KNOWLEDGEBASE.findTop4Posts(query)
        if (posts.size == 0) {
            event.reply("No post was found. This is likely an error and shouldn't happen.").setEphemeral(true).queue()
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
