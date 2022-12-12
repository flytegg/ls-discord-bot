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

    fun findLecture(query: String, amount: Int = 3): List<Lecture> {
        val scores: MutableMap<Lecture, Double> = mutableMapOf()

        lectures.forEach {
            val title = sanitizeString(it.title)
            val description = sanitizeString(it.description)
            var score = (0.8 * jaroWinkler.apply(query, title)) + (0.2 * jaroWinkler.apply(query, description))

            val titleWords: List<String> = title.split(" ")
            val descriptionWords: List<String> = description.split(" ")

            query.split(" ").forEach { word ->
                if(titleWords.contains(word)) {
                    score += if (titleWords.size == 1 && query.split(" ").size == 1) 1.25 else 0.55
                }
                if(descriptionWords.contains(word)) {
                    score += 0.25
                }
            }
            scores[it] = score
        }

        return scores
            .toList().sortedWith { o1, o2 ->
                o1.second compareTo o2.second
            }
            .map { it.first }
            .take(amount)
    }

    fun findQuiz(query: String, amount: Int = 3): List<Quiz> {
        val scores: MutableMap<Quiz, Double> = mutableMapOf()

        quizzes.forEach {
            var score = jaroWinkler.apply(query, it.title)

            val titleWords: List<String> = it.title.split(" ")

            query.split(" ").forEach { word ->
                if(titleWords.contains(word)) {
                    score += 0.2
                }
            }
            scores[it] = score
        }

        return scores
            .toList().sortedWith { o1, o2 ->
                o1.second compareTo o2.second
            }
            .map { it.first }
            .take(amount)

    }

    private fun sanitizeString(string: String): String {
        return string.lowercase(Locale.getDefault())
            .replace("\n", " ") // Replace line breaks with spaces
            .replace("\\b(the|a|an)\\b".toRegex(), "") // Remove determiners
            .replace("[^\\w\\s]".toRegex(), "") // Only leave valid characters
            .replace("\\s+".toRegex(), " ") // Clean up stacked spaces;
    }
}