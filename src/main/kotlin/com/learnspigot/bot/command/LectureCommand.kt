package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.util.LectureSearcher
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild

class LectureCommand(private val guild: Guild, private val bot: JDA, private val searcher: LectureSearcher) {

    fun searchLecturesCommand() {
        guild.upsertCommand("lecture", "Search for a lecture") {
            restrict(guild = true)
            option<String>("query", "The query for searching", true)
            bot.onCommand("lecture") {
                it.deferReply().queue()
                val lectures = searcher.findLecture(it.getOption("query")!!.asString, amount = 4).toMutableList()
                it.hook.editOriginalEmbeds(Embed {
                    val topLecture = lectures[0]
                    title = topLecture.title
                    color = EMBED_COLOR
                    description = """
                        ${topLecture.description}

                        [Click here to watch the lecture](${topLecture.url})
                        
                        **Not what you're looking for? How about:**
                    """.trimIndent()
                    description += "\n"
                    lectures.removeFirst()
                    lectures.forEach {
                        description += "\n• [${it.title}](${it.url})"
                    }
                }).queue()
            }
        }.queue()
    }

    fun searchQuizzesCommand() {
        guild.upsertCommand("quiz", "Search for a quiz") {
            restrict(guild = true)
            option<String>("query", "The query for searching", true)
            bot.onCommand("quiz") {
                it.deferReply().queue()
                val quizzes = searcher.findQuiz(it.getOption("query")!!.asString, amount = 4).toMutableList()

                it.hook.editOriginalEmbeds(Embed {
                    val topQuiz = quizzes[0]
                    title = topQuiz.title
                    color = EMBED_COLOR
                    description = """
                        To pass you need to get at least ${topQuiz.passPercentage}%

                        [Click to take the quiz](${topQuiz.url})
                        
                        **Not what you're looking for? How about:**
                    """.trimIndent()
                    description += "\n"
                    quizzes.removeFirst()
                    quizzes.forEach {
                        description += "\n• [${it.title}](${it.url})"
                    }
                }).queue()
            }
        }.queue()
    }

}