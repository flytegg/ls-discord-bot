package com.learnspigot.bot.util

import com.mongodb.client.MongoClients

object Mongo {

    private val client = MongoClients.create(System.getenv("MONGO_URI"))
    private val database = client.getDatabase("learnspigot")

    val userCollection = database.getCollection(System.getenv("MONGO_COLLECTION"))

}