package com.learnspigot.bot.util

import com.learnspigot.bot.entity.Lecture
import com.learnspigot.bot.entity.Quiz
import com.learnspigot.bot.http.UdemyService
import kotlinx.coroutines.*
import org.apache.commons.text.similarity.JaroWinklerDistance
import java.util.*
import kotlin.time.Duration.Companion.minutes


@OptIn(DelicateCoroutinesApi::class)
class LectureSearcher(private val udemy: UdemyService) {

    private val matcher = WordMatcher()

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

    fun findLecture(_query: String, amount: Int = 3) = matcher.getTopLectures(sanitizeString(_query), lectures, amount)

    fun findQuiz(query: String, amount: Int = 3) = matcher.getTopQuizzes(sanitizeString(query), quizzes, amount)

    private fun sanitizeString(string: String): String {
        return string.lowercase(Locale.getDefault())
            .replace("\n", " ") // Replace line breaks with spaces
            .replace("\\b(the|a|an)\\b".toRegex(), "") // Remove determiners
            .replace("[^\\w\\s]".toRegex(), "") // Only leave valid characters
            .replace("\\s+".toRegex(), " ") // Clean up stacked spaces;
    }
}
