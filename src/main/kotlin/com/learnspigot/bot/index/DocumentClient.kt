package com.learnspigot.bot.index

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.AutoCloseable

class DocumentClient(
    val url: String,
) : AutoCloseable {
    private var doc: Document? = Jsoup.connect(url).get()

    fun openPage(): Document = doc ?: throw IllegalStateException("Document accessed after close")

    override fun close() {
        if (doc == null) error("Document already closed")
        doc = null
    }
}
