package com.learnspigot.bot.util

import com.learnspigot.bot.Environment
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

object Mongo {

    private val client = MongoClients.create(Environment.get("MONGO_URI"))
    private val database = client.getDatabase("learnspigot")

    val userCollection: MongoCollection<Document> = database.getCollection(Environment.get("MONGO_USERS_COLLECTION"))
    val starboardCollection: MongoCollection<Document> = database.getCollection(Environment.get("MONGO_STARBOARD_COLLECTION"))
    val docsCollection: MongoCollection<Document> = database.getCollection(Environment.get("MONGO_SPIGOT_DOCS_COLLECTION"))

}