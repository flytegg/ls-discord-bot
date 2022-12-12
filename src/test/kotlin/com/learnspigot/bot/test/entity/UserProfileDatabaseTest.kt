package com.learnspigot.bot.test.entity

import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.entity.UserProfile
import com.mongodb.client.MongoClients
import com.mongodb.client.result.DeleteResult
import dev.morphia.Datastore
import dev.morphia.Morphia
import dev.morphia.query.experimental.filters.Filters
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
internal class UserProfileDatabaseTest {

    companion object {
        const val TEST_ID = "testId"
        const val TEST_UDEMY_URL = "https://udemy.com/user/testId"
        const val ALT_TEST_UDEMY_URL = "https://udemy.com/user/testId2"
    }

    lateinit var datastore: Datastore
    var hasSaved = false

    @BeforeAll
    fun startConnection() {
        datastore = Morphia.createDatastore(MongoClients.create(System.getenv("MONGO_URI")!!), System.getenv("MONGO_DB")!!)
        datastore.mapper.mapPackage("com.learnspigot.bot.entity")
    }

    @Test
    @Order(1)
    fun testSaving() {
        assertDoesNotThrow {
            val profile = UserProfile(TEST_ID, TEST_UDEMY_URL)
            datastore.save(profile)
            hasSaved = true
        }
    }

    @Test
    @Order(2)
    fun testQuerying() {
        assertTrue(hasSaved)
        val profile: UserProfile = datastore.findUserProfile(TEST_ID)
        assertNotNull(profile)
        assertEquals(TEST_UDEMY_URL, profile.udemyUrl)
    }

    @Test
    @Order(3)
    fun testEditing() {
        assertDoesNotThrow {
            val profile: UserProfile = datastore.findUserProfile(TEST_ID)
            profile.udemyUrl = ALT_TEST_UDEMY_URL
            datastore.save(profile)
        }

        val profile: UserProfile = datastore.findUserProfile(TEST_ID)
        assertEquals(ALT_TEST_UDEMY_URL, profile.udemyUrl)
    }

    @Test
    @Order(4)
    fun testDeleting() {
        assertEquals(DeleteResult.acknowledged(1), datastore.find(UserProfile::class.java).filter(Filters.eq("id", TEST_ID)).delete())
    }
}