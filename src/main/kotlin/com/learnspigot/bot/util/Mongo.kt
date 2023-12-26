package com.learnspigot.bot.util

import com.learnspigot.bot.Environment
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

object Mongo {

    private val client = MongoClients.create(Environment.get("MONGO_URI"))
    private val database = client.getDatabase(Environment.get("MONGO_DATABASE"))

    val userCollection: MongoCollection<Document> = database.getCollection("users")
    val starboardCollection: MongoCollection<Document> = database.getCollection("starboard")
    val docsCollection: MongoCollection<Document> = database.getCollection("spigot-docs")
    val countingCollection: MongoCollection<Document> = database.getCollection("counting")


}