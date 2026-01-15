package com.learnspigot.bot.videos.udemy

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor
import java.util.function.Consumer

class UdemyCommand {

    @Command("udemy")
    @Description("Search for a lecture in the course")
    fun onUdemyCommand(
        actor: SlashCommandActor,
        @Description("Lecture title or keywords") query: String
    ) {
        val event = actor.commandEvent()
        val lectures = Registry.UDEMY.findLectures(query, 4)
        val topLecture = lectures.removeFirst()
        val suggestions = StringBuilder()
        lectures.forEach(Consumer { lecture: UdemyItem ->
            suggestions.append("- [").append(lecture.title).append("](").append(lecture.url()).append(")\n")
        })
        event.replyEmbeds(
            embed()
                .setTitle(topLecture.title, topLecture.url())
                .setDescription((topLecture.description + "\n\n[Click here to watch the lecture](" + topLecture.url()) + ")")
                .addField("Not quite what you're looking for?", suggestions.toString(), false)
                .build()
        ).queue()
    }

}