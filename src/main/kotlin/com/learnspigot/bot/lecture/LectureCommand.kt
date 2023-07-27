package com.learnspigot.bot.lecture

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.function.Consumer

class LectureCommand {

    @Inject
    private lateinit var lectureRegistry: LectureRegistry

    @Command(
        name = "lecture",
        description = "Search for a lecture in the course"
    )
    fun onLectureCommand(
        event: SlashCommandInteractionEvent,
        @Description("Lecture title or keywords") query: String
    ) {
        val lectures = lectureRegistry.findLectures(query, 4)
        val topLecture = lectures.removeFirst()
        val suggestions = StringBuilder()
        lectures.forEach(Consumer { lecture: Lecture ->
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