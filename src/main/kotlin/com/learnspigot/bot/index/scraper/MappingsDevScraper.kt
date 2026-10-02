package com.learnspigot.bot.index.scraper

import com.learnspigot.bot.Bot
import com.learnspigot.bot.index.DocumentClient
import com.learnspigot.bot.index.IndexEntry
import com.learnspigot.bot.index.IndexEntryKind
import org.jsoup.nodes.Document
import java.util.concurrent.CountDownLatch
import java.util.function.Consumer

/**
 * Scrapes https://mappings.dev/ specifically
 */
class MappingsDevScraper(val entrypoint: String) : Scraper {

    val mainUrl = entrypoint.substringBeforeLast('/') // strip "/index.html"

    fun String.packageToUrl() = "$mainUrl/${this.replace(".","/")}/index.html"
    fun String.classToUrl(`package`: String) = "$mainUrl/${`package`.replace(".", "/")}/$this"

    val found: MutableList<IndexEntry> = mutableListOf()
    val foundSet: MutableSet<String> = mutableSetOf()

    override fun scrape(afterFinish: Consumer<List<IndexEntry>>) {
        Scraper.mainExecutor.submit {
            runCatching {
                DocumentClient(entrypoint).use { client ->
                    val page = client.openPage()
                    if(canLog) println("[SCRAPE] starting to scrape $entrypoint")

                    val packages = page.select("table.M > tbody > tr > td > a")
                        .filter { !it.ownText().startsWith("net.minecraft.client") }
                        .map { it.ownText() }
                    val latch = CountDownLatch(packages.size)
                    for (s in packages) scrapePackage(s.packageToUrl(),latch) {
                        if(foundSet.add(it.name)) found.add(it)
                    }
                    latch.await()

                    if(canLog) println("[SCRAPE] finished scraping $entrypoint")
                    afterFinish.accept(found)
                }
            }.exceptionOrNull()?.printStackTrace()
        }

    }

    fun scrapePackage(url:String,latch: CountDownLatch,adder: Consumer<IndexEntry>) {
        Scraper.mainExecutor.submit {
                DocumentClient(url).use { client ->
                    runCatching {
                    val page = client.openPage()
                    if(canLog) println("[SCRAPE] starting to scrape $url")

                    val classNames = page.select("table.M > tbody > tr > td > a")
                        .map { it.ownText() }
                    val subLatch = CountDownLatch(classNames.size)

                    if(canLog) println("[SCRAPE] found ${classNames.size} entries")
                    for (s in classNames)
                        scrapeClassPage(s.classToUrl(page.name!!),subLatch,adder)
                    subLatch.await()
                    latch.countDown()
                    }.exceptionOrNull()?.printStackTrace()?.also { latch.countDown() }
                }
        }
    }

    fun scrapeClassPage(url:String,latch: CountDownLatch, adder:Consumer<IndexEntry>) {
        Scraper.poolExecutor.submit {
            runCatching {
                DocumentClient(url).use { client ->
                    val page = client.openPage()
                    if(canLog) println("[SCRAPE] starting to scrape $url")

                    val fullClassName = page.name!!
                    val modifiers = page.selectFirst(".A > p:nth-child(1)")
                        ?.ownText()?.replace(Regex("\\s+${fullClassName.substringAfterLast('.')}"), "") ?: ""
                    val kind =
                        if(modifiers.contains("abstract class")||
                            modifiers.contains("abstract static class")) IndexEntryKind.ABSTRACT_CLASS
                        else if(modifiers.contains("class")) IndexEntryKind.CLASS
                        else if(modifiers.contains("interface")) IndexEntryKind.INTERFACE
                        else if(modifiers.contains("enum")) IndexEntryKind.ENUM
                        else if(modifiers.contains("record")) IndexEntryKind.RECORD
                        else if(modifiers.contains("@interface")) IndexEntryKind.ANNOTATION
                        else IndexEntryKind.UNKNOWN

                    page.select("main > table:first-of-type > tbody > tr > td.F")
                        .map { it.ownText()}
                        .forEach { entryName ->
                            adder.accept(
                                IndexEntry(
                                    if (entryName.contains(".")) entryName.substringAfterLast('.') else entryName,
                                    entryName, url, kind, entrypoint
                                )
                            )
                        }
                    latch.countDown()
                }
            }.exceptionOrNull()?.printStackTrace()?.also { latch.countDown() }
        }
    }

    val Document.name get() = selectFirst("meta[name=\"og:title\"]")?.attr("content")

}