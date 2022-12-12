package com.learnspigot.bot.dbmigrator.test

import com.learnspigot.bot.dbmigrator.migrations.UserProfileMigrator
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.collections.set

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class UserProfileMigratorTest {

    companion object {
        const val TEST_ID: Long = 503521520531013633
        const val TEST_UDEMY_ID: Long = 0L // 213019002

        const val TEST_REP_TIMESTAMP = 1667302256155
        const val TEST_REP_FROM_ID = 503521520531013633
        const val TEST_REP_FROM_CHANNEL_ID = 1046467722218831903

        const val TEST_MESSAGE_HISTORY_ID = 1046467722218831903
        const val TEST_MESSAGE_HISTORY_AUTHOR_ID = 824581326001668147
        const val TEST_MESSAGE_HISTORY_CONTENT = "Test message content"
        const val TEST_MESSAGE_HISTORY_TIMESTAMP = 1667296433772
        const val TEST_MESSAGE_HISTORY_CHANNEL_ID = 397536574448861196
        const val TEST_MESSAGE_HISTORY_CHANNEL_NAME = "test-channel-name"
    }

    private lateinit var client: MongoClient
    private lateinit var database: MongoDatabase

    private lateinit var oldCollection: MongoCollection<Document>
    private lateinit var messageHistoryCollection: MongoCollection<Document>
    private lateinit var newCollection: MongoCollection<Document>

    private lateinit var doc: Document

    @BeforeAll
    fun setupConnection() {
        client = MongoClients.create(System.getenv("MONGO_URI"))
        database = client.getDatabase(System.getenv("MONGO_DB"))

        oldCollection = database.getCollection("old_users")
        messageHistoryCollection = database.getCollection("message_history")
        newCollection = database.getCollection("new_users")

        val oldUserDoc = Document("id", TEST_ID)
        oldUserDoc["udemyId"] = TEST_UDEMY_ID
        oldUserDoc["reputation"] = listOf(Document(mapOf(
            "timestamp" to TEST_REP_TIMESTAMP,
            "fromId" to TEST_REP_FROM_ID,
            "postId" to TEST_REP_FROM_CHANNEL_ID
        )))
        oldCollection.insertOne(oldUserDoc)

        val oldMessageHistoryDoc = Document("id", TEST_ID)
        oldMessageHistoryDoc["messages"] = listOf(Document(mapOf(
            "id" to TEST_MESSAGE_HISTORY_ID,
            "authorId" to TEST_MESSAGE_HISTORY_AUTHOR_ID,
            "content" to TEST_MESSAGE_HISTORY_CONTENT,
            "timestamp" to TEST_MESSAGE_HISTORY_TIMESTAMP,
            "channel" to Document(mapOf("id" to TEST_MESSAGE_HISTORY_CHANNEL_ID, "name" to TEST_MESSAGE_HISTORY_CHANNEL_NAME))
        )))

        messageHistoryCollection.insertOne(oldMessageHistoryDoc)
    }

    @Test
    @Order(0)
    fun testMigration() {
        assertDoesNotThrow {
            UserProfileMigrator().scanDatabase(database, targetCollection = newCollection)
        }
        assertTrue {
            newCollection.find(Filters.eq("_id", TEST_ID.toString())).first() != null
        }
        doc = newCollection.find(Filters.eq("_id", TEST_ID.toString())).first()!!
    }

    @Test
    @Order(1)
    fun testData() {
        assertEquals(TEST_ID.toString(), doc["_id"])
        assertEquals("UserProfile", doc["_t"])
        assertTrue(doc.containsKey("udemy"))
    }

    @Test
    @Order(2)
    fun testReputation() {
        val reputation = doc.getList("reputation", Document::class.java)[0]
        assertNotNull(reputation)
        assertEquals("ReputationPoint", reputation["_t"])
        assertEquals(TEST_REP_TIMESTAMP, reputation["epochTimestamp"])
        assertEquals(TEST_REP_FROM_ID.toString(), reputation["fromMemberId"])
        assertEquals(TEST_REP_FROM_CHANNEL_ID.toString(), reputation["postId"])
    }

    @Test
    @Order(2)
    fun testMessageHistory() {
        val messageHistory = doc.getList("messageHistory", Document::class.java)[0]
        assertNotNull(messageHistory)
        assertEquals("SerializedMessage", messageHistory["_t"])
        assertEquals(TEST_MESSAGE_HISTORY_TIMESTAMP, messageHistory["timestamp"])
        assertEquals(TEST_MESSAGE_HISTORY_CHANNEL_ID.toString(), messageHistory["channelId"])
        assertEquals(TEST_MESSAGE_HISTORY_CONTENT, messageHistory["content"])
        assertEquals(TEST_MESSAGE_HISTORY_ID.toString(), messageHistory["id"])
    }



    @AfterAll
    fun teardownDatabase() {
        //newCollection.drop()
        oldCollection.drop()
        messageHistoryCollection.drop()
    }
}