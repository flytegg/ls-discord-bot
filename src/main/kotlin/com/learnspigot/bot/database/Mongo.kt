package com.learnspigot.bot.database

import com.learnspigot.bot.Environment
import com.learnspigot.bot.database.profile.Profile
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import org.bson.Document

object Mongo {

    private val client = MongoClients.create(Environment.MONGO_URI)
    private val database = client.getDatabase(Environment.MONGO_DATABASE)

    val userCollection: MongoCollection<Profile> = database.getCollection("users", Profile::class.java)
    val starboardCollection: MongoCollection<Document> = database.getCollection("starboard")
    val countingCollection: MongoCollection<Document> = database.getCollection("counting")

}