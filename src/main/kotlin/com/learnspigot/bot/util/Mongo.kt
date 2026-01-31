package com.learnspigot.bot.util

import com.learnspigot.bot.Bot
import com.mongodb.client.MongoCollection
import org.bson.Document
import org.litote.kmongo.KMongo

object Mongo {

    private val client = KMongo.createClient(Bot.fromEnv("MONGO_URI")).also {
        println("Connected to MongoDB with URI: ${Bot.fromEnv("MONGO_DATABASE")}")
    }
    private val database = client.getDatabase(Bot.fromEnv("MONGO_DATABASE"))

    val userCollection: MongoCollection<Document> = database.getCollection("users")
    val starboardCollection: MongoCollection<Document> = database.getCollection("starboard")
    val docsCollection: MongoCollection<Document> = database.getCollection("spigot-docs")
    val countingCollection: MongoCollection<Document> = database.getCollection("counting")
    val pendingVerificationsCollection: MongoCollection<Document> = database.getCollection("pending-verifications")
    val countingBansCollection: MongoCollection<Document> = database.getCollection("counting-bans")
}