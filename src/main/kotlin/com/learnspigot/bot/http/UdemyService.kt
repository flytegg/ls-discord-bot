package com.learnspigot.bot.http

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.entity.Lecture
import com.learnspigot.bot.entity.Quiz
import com.learnspigot.bot.entity.UdemyProfile
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpRequest


class UdemyService : HttpService() {

    companion object {
        const val ENDPOINT = "https://www.udemy.com/api-2.0"
    }

    fun studentOwnsCourse(udemyProfile: String): Boolean {
        val profile = lookupProfile(udemyProfile) ?: return false
        profile.ownedCourses.forEach {
            if(it.id == LearnSpigotBot.LEARN_SPIGOT_COURSE_ID.toString()) return true
        }
        return false
    }

    fun lookupProfile(url: String): UdemyProfile? {
        if(url == "https://www.udemy.com/null") return null
        val id = with(sendStringRequest(buildRequest(url))) {
            body().substring(body().indexOf("id&quot;:") + 9).split(",&quot")[0]
        }
        with(sendJsonRequest(buildRequest("$ENDPOINT/users/$id"))) {
            val data = body().asJsonObject
            return UdemyProfile(id, url, data["display_name"].asString, lookupCourses(id))
        }
    }

    private fun lookupCourses(id: String): List<UdemyProfile.Course> {
        with(sendJsonRequest(buildRequest("$ENDPOINT/users/$id/subscribed-profile-courses/"))) {
            return body().asJsonObject["results"].asJsonArray
                .map { it.asJsonObject }
                .map { UdemyProfile.Course(it["id"].asString, it["title"].asString, it["url"].asString) }
        }
    }

    fun lectures(): List<Lecture> {
        val lectures: MutableList<Lecture> = mutableListOf()
        with(sendJsonRequest(buildRequest("$ENDPOINT/courses/${LearnSpigotBot.LEARN_SPIGOT_COURSE_ID}/public-curriculum-items"))) {
            lectures.addAll(body().asJsonObject["results"].asJsonArray
                .map { it.asJsonObject }
                .filter { it["_class"].asString == "lecture" }
                .map { Lecture(it["id"].asLong.toString(), it["title"].asString,
                    it["description"].asString
                        .replace("<li>", "\n\u2022").replace(Regex("<.*?>"), "").replace("&amp;", "&")) })
            var body = body().asJsonObject
            while (!body["next"].isJsonNull) {
                body = sendJsonRequest(buildRequest(body["next"].asString)).body().asJsonObject
                lectures.addAll(body.asJsonObject["results"].asJsonArray
                    .map { it.asJsonObject }
                    .filter { it["_class"].asString == "lecture" }
                    .map { Lecture(it["id"].asLong.toString(), it["title"].asString,
                        it["description"].asString
                            .replace("<li>", "\n\u2022").replace(Regex("<.*?>"), "").replace("&amp;", "&")) })
            }
        }
        return lectures
    }

    fun quizzes(): List<Quiz> {
        val lectures: MutableList<Quiz> = mutableListOf()
        with(sendJsonRequest(buildRequest("$ENDPOINT/courses/${LearnSpigotBot.LEARN_SPIGOT_COURSE_ID}/public-curriculum-items"))) {
            lectures.addAll(body().asJsonObject["results"].asJsonArray
                .map { it.asJsonObject }
                .filter { it["_class"].asString == "quiz" }
                .map { Quiz(it["id"].asLong.toString(), it["title"].asString, it["pass_percent"].asInt) })

            var body = body().asJsonObject
            while (!body["next"].isJsonNull) {
                body = sendJsonRequest(buildRequest(body["next"].asString)).body().asJsonObject
                lectures.addAll(body.asJsonObject["results"].asJsonArray
                    .map { it.asJsonObject }
                    .filter { it["_class"].asString == "quiz" }
                    .map { Quiz(it["id"].asLong.toString(), it["title"].asString, it["pass_percent"].asInt) })
            }
        }
        return lectures
    }

    override fun buildRequest(url: String): HttpRequest {
        return try {
            HttpRequest.newBuilder()
                .uri(URI(url))
                .GET()
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer ${System.getenv("UDEMY_BEARER")!!}")
                .setHeader("Cookie", "client_id=${System.getenv("UDEMY_CLIENT_ID")!!}; access_token=${System.getenv("UDEMY_BEARER")!!}")
                .build()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

}