package com.learnspigot.bot.util

import com.learnspigot.bot.entity.CourseContent
import com.learnspigot.bot.entity.Lecture
import com.learnspigot.bot.entity.Quiz
import com.learnspigot.bot.http.UdemyService
import kotlinx.coroutines.*
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.model.BoundExtractedResult
import org.apache.commons.text.similarity.JaroWinklerDistance
import java.util.*
import kotlin.time.Duration.Companion.minutes


@OptIn(DelicateCoroutinesApi::class)
class LectureSearcher(private val udemy: UdemyService) {

    private val jaroWinkler = JaroWinklerDistance()

    private lateinit var lectures: List<Lecture>
    private lateinit var lectureUpdateJob: Job

    private lateinit var quizzes: List<Quiz>
    private lateinit var quizzesUpdateJob: Job

    init {
        GlobalScope.launch {
            lectureUpdateJob = async {
                while (true) {
                    lectures = udemy.lectures()
                    delay(30.minutes)
                }
            }

            quizzesUpdateJob = async {
                while (true) {
                    quizzes = udemy.quizzes()
                    delay(30.minutes)
                }
            }
        }
    }


    fun findLecture(_query: String, amount: Int = 4): List<Lecture> {
        val query = sanitizeString(_query)
        return fuzzySearchCourseContent(query, lectures, amount) as List<Lecture>
    }

    fun findQuiz(query: String, amount: Int = 4): List<Quiz> {
        return fuzzySearchCourseContent(query, quizzes, amount) as List<Quiz>
    }
    private fun fuzzySearchCourseContent(query: String, list: List<CourseContent>, amount: Int): List<CourseContent> {
        val matches: MutableList<BoundExtractedResult<CourseContent>>? =
            FuzzySearch.extractTop(query, list, { it.title }, amount);
        return matches?.map { it.referent } ?: listOf()
    }

    private fun sanitizeString(string: String): String {
        return string.lowercase(Locale.getDefault())
            .replace("\n", " ") // Replace line breaks with spaces
            .replace("\\b(the|a|an)\\b".toRegex(), "") // Remove determiners
            .replace("[^\\w\\s]".toRegex(), "") // Only leave valid characters
            .replace("\\s+".toRegex(), " ") // Clean up stacked spaces;
    }
}
