package com.learnspigot.bot.dbmigrator.migrations

import com.learnspigot.bot.dbmigrator.http.UdemyService
import com.learnspigot.bot.dbmigrator.util.cloneDocument
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import kotlin.collections.set

class UserProfileMigrator: Migrator {

    override val collectionName: String = "users"

    override fun detect(document: Document, collection: MongoCollection<Document>): Boolean = document.containsKey("udemyId") && !document.containsKey("_t")

    override fun migrateDocument(document: Document, database: MongoDatabase): Document {
        val newDoc = cloneDocument {
            inputDoc = document

            ignoreKeys += arrayOf("id", "reputation", "udemyId")

            documentMutator { newDoc, oldDoc ->
                newDoc["udemy"] = UdemyService().getUdemyUrlById(oldDoc.getLong("udemyId").toString())
            }

            documentMutator { newDoc, oldDoc ->
                newDoc["_id"] = oldDoc["id"].toString()
            }
            documentMutator { newDoc, _ ->
                newDoc["_t"] = "UserProfile"
            }
            documentMutator { newDoc, oldDoc ->
                newDoc["reputation"] = oldDoc.getList("reputation", Document::class.java).map {
                    val doc = Document("_t", "ReputationPoint")
                    doc["epochTimestamp"] = it["timestamp"]
                    doc["fromMemberId"] = it["fromId"].toString()
                    doc["postId"] = it["postId"].toString()

                    doc
                }
            }

            documentMutator { newDoc, _ ->
                if(database.listCollectionNames().contains("message_history")) {
                    val messageHistoryCollection = database.getCollection("message_history")
                    val messageHistory = messageHistoryCollection.find(Filters.eq("id", document["id"])).first()
                    if(messageHistory == null) {
                        newDoc["messageHistory"] = emptyArray<Document>()
                        return@documentMutator
                    }

                    newDoc["messageHistory"] = messageHistory.getList("messages", Document::class.java)
                        .map {
                            cloneDocument {
                                inputDoc = it
                                ignoreKeys += arrayOf("id", "channel", "authorId")

                                documentMutator { newDoc, _ ->
                                    newDoc["_t"] = "SerializedMessage"
                                }

                                documentMutator { newDoc, oldDoc ->
                                    newDoc["id"] = oldDoc["id"].toString()
                                }

                                documentMutator { newDoc, oldDoc ->
                                    newDoc["channelId"] = oldDoc.get("channel", Document::class.java)["id"].toString()
                                }
                            }
                        }
                }else {
                    newDoc["messageHistory"] = emptyArray<Document>()
                }
            }
        }

        return newDoc
    }


}