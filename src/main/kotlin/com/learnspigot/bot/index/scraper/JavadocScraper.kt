package com.learnspigot.bot.index.scraper

import com.learnspigot.bot.index.DocumentClient
import com.learnspigot.bot.index.IndexEntry
import com.learnspigot.bot.index.IndexEntryKind
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer
import java.util.regex.Pattern

/**
 * Scrapes any Javadoc
 */
class JavadocScraper(
    val entrypoint: String,
) : Scraper {
    private val found: MutableList<IndexEntry> = mutableListOf()

    override fun scrape(afterFinish: Consumer<List<IndexEntry>>) {
        Scraper.mainExecutor.submit {
            runCatching {
                if (canLog) println("[${Thread.currentThread().name}] starting to scrape $entrypoint")

                val pageClient = DocumentClient(entrypoint)
                val mainPage = pageClient.openPage()
                val homeUrl = entrypoint.substring(0..entrypoint.lastIndexOf('/'))

                if (canLog) println("[${Thread.currentThread().name}] opened $entrypoint")
                val classes: Elements =
                    mainPage.select(
                        "#all-classes-table\\.tabpanel > .summary-table > .col-first > a, .typeSummary > tbody > tr > .colFirst > a,.indexContainer > ul > li > a",
                    )

                val latch = CountDownLatch(classes.size)
                for (element in classes) {
                    val href = element.attr("href")
                    if (href.isNotBlank()) {
                        if (href.contains("https")) {
                            latch.countDown()
                        } else {
                            Scraper.poolExecutor.submit {
                                scrapeClassPage("$homeUrl$href", latch) { found.add(it) }
                            }
                        }
                    }
                }

                latch.await()
                pageClient.close()
                if (canLog) println("[${Thread.currentThread().name}] finished")
                afterFinish.accept(found)
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    // group1: class type, group2: simple class name
    val classDeclarationRegex: Pattern = Pattern.compile("(enum|interface|class|record|annotation type):\\s+([A-Za-z_$][A-Za-z_$0-9]+)")

    fun scrapeClassPage(
        url: String,
        latch: CountDownLatch,
        adder: Consumer<IndexEntry>,
    ) {
        runCatching {
            if (canLog) println("[${Thread.currentThread().name}] scraping class $url")
            val client = DocumentClient(url)
            val page = client.openPage()

            val classDeclaration: List<List<String>>? =
                page
                    .selectFirst("meta[name=\"description\"]")
                    ?.attr("content")
                    ?.let { str ->
                        val matcher = classDeclarationRegex.matcher(str)
                        val list = mutableListOf<List<String>>()
                        while (matcher.find()) {
                            list.add(listOf(matcher.group(), matcher.group(1), matcher.group(2)))
                        }
                        list
                    }
                    ?: page
                        .selectFirst("div.description > ul.blockList > li.blockList > pre")
                        ?.let { element ->
                            listOf(
                                listOf(
                                    element.text(),
                                    element
                                        .textNodes()
                                        .first()
                                        .nodeValue()
                                        .replace(Regex("public|private|sealed|non-sealed"), ""),
                                    element.selectFirst("span.typeNameLabel")!!.text(),
                                ),
                            )
                        }

            val className = classDeclaration?.joinToString(".") { it[2] } ?: ""
            val classType = classDeclaration?.last()[1]?.trim() ?: ""

            if (className.isNotBlank()) {
                val modifiers =
                    page
                        .selectFirst(
                            "#class-description > div > div.type-signature > span.modifiers, div.description > ul.blockList > li.blockList > pre",
                        )?.ownText()
                        ?.trim()

                val extends =
                    page
                        .select(".inheritance > a")
                        .map { listOf(it.attr("href"), it.ownText().trim()) }

                val kind =
                    when {
                        modifiers?.contains("abstract class") == true -> IndexEntryKind.ABSTRACT_CLASS
                        classType == "interface" || modifiers?.contains("interface") == true -> IndexEntryKind.INTERFACE
                        extends.any { it[1] == "org.bukkit.event.Event" } -> IndexEntryKind.EVENT
                        classType == "class" || modifiers?.contains("class") == true -> IndexEntryKind.CLASS
                        classType == "enum" || modifiers?.contains("enum") == true -> IndexEntryKind.ENUM
                        classType == "record" || modifiers?.contains("record") == true -> IndexEntryKind.RECORD
                        classType == "annotation type" -> IndexEntryKind.ANNOTATION
                        else -> IndexEntryKind.UNKNOWN
                    }

                adder.accept(
                    IndexEntry(
                        className,
                        "${page.packageName}.$className",
                        url,
                        kind,
                        entrypoint,
                    ),
                )
                if (canLog) println("[${Thread.currentThread().name}] added $className")
            } else {
                System.err.println("[${Thread.currentThread().name}] $url gave nothing")
            }

            client.close()
            latch.countDown()
        }.exceptionOrNull()?.printStackTrace()
    }

    val packageRegex: Pattern = Pattern.compile("package: ([A-Za-z_0-9$]+(?:\\.[A-Za-z_0-9$]+)+)")

    val Document.packageName get() =
        selectFirst("meta[name=\"description\"]")
            ?.attr("content")
            ?.apply {
                val matcher = packageRegex.matcher(this)
                if (matcher.find()) return matcher.group(1)
            }
            ?: selectFirst("main > .header > .subTitle > a")?.ownText()?.trim()
            ?: selectFirst("div.contentContainer > ul.inheritance")
                ?.text()
                ?.split(" ")
                ?.last()
                ?.substringBeforeLast('.')
}
