package com.learnspigot.bot.dbmigrator.migrations

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

interface Migrator {

    /**
     * The name of the collection migrated documents should be stored in
     */
    val collectionName: String

    fun detect(document: Document, collection: MongoCollection<Document>): Boolean

    fun migrateDocument(document: Document, database: MongoDatabase): Document

    fun scanDatabase(database: MongoDatabase, targetCollection: MongoCollection<Document> = database.getCollection(collectionName)) {
        database.listCollectionNames().forEach { collectionName ->
            val collection = database.getCollection(collectionName)

            collection.find().forEach {
                if(detect(it, collection)) {
                    runCatching {
                        targetCollection.insertOne(migrateDocument(it, database))
                    }
                }
            }
        }
    }
}