package com.learnspigot.bot.dbmigrator.util

import org.bson.Document

typealias DocumentMutator = (newDoc: Document, oldDoc: Document) -> Unit
typealias KeyNameChanger = DocumentCloner.KeyNameChangerBuilder.() -> Unit
class DocumentCloner {

    lateinit var inputDoc: Document
    var ignoreKeys: Array<String> = emptyArray()

    private val keyNameChanges: MutableMap<String, String> = mutableMapOf()
    private val documentMutators: MutableList<DocumentMutator> = mutableListOf()

    fun documentMutator(mutator: DocumentMutator) {
        documentMutators.add(mutator)
    }

    fun keyNameChange(changer: KeyNameChanger) {
        changer.invoke(KeyNameChangerBuilder())
    }

    inner class KeyNameChangerBuilder {
        operator fun String.plusAssign(changedKey: String) {
            keyNameChanges[this] = changedKey
        }
    }

    internal fun cloneDocument(): Document {
        val newDoc = Document()

        inputDoc.keys.forEach {
            if(!ignoreKeys.contains(it)) {
                val key: String = keyNameChanges.keys.find { key -> key ==  it } ?: it
                newDoc[key] = inputDoc[key]
            }
        }
        documentMutators.forEach {
            it.invoke(newDoc, inputDoc)
        }
        return newDoc
    }
}

fun cloneDocument(consumer: DocumentCloner.() -> Unit): Document {
    val cloner = DocumentCloner()
    consumer.invoke(cloner)
    return cloner.cloneDocument()
}