package com.learnspigot.bot.index.scraper

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.PackageDeclaration
import com.github.javaparser.ast.body.AnnotationDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.EnumDeclaration
import com.github.javaparser.ast.body.RecordDeclaration
import com.learnspigot.bot.index.GitHubContentsResponse
import com.learnspigot.bot.index.Http
import com.learnspigot.bot.index.IndexEntry
import com.learnspigot.bot.index.IndexEntryKind
import java.util.function.Consumer

class GitHubScraper(
    val user: String,
    val repo: String,
) : Scraper {
    val entrypoint = "https://api.github.com/repos/$user/$repo/contents/src/main/java"

    private val repository = "https://github.com/$user/$repo"
    private val found: MutableList<IndexEntry> = mutableListOf()

    override fun scrape(afterFinish: Consumer<List<IndexEntry>>) {
        Scraper.mainExecutor.submit {
            runCatching {
                if (canLog) println("[${Thread.currentThread().name}] starting to scrape $user/$repo")
                for (response in Http.getJson(entrypoint, Array<GitHubContentsResponse>::class.java) ?: emptyArray()) {
                    if (response.type == "file" && response.name.endsWith(".java")) scrapeFile(response)
                    if (response.type == "dir") scrapeDirectory(response)
                }
                afterFinish.accept(found)
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    fun scrapeDirectory(dirContents: GitHubContentsResponse) {
        if (canLog) println("[${Thread.currentThread().name}] opening directory ${dirContents.url}")
        val responses = Http.getJson(dirContents.url, Array<GitHubContentsResponse>::class.java) ?: emptyArray()
        for (response in responses) {
            when (response.type) {
                "file" if response.name.endsWith(".java") -> scrapeFile(response)
                "dir" -> scrapeDirectory(response)
            }
        }
    }

    fun scrapeFile(contents: GitHubContentsResponse) {
        if (canLog) println("[${Thread.currentThread().name}] opening file ${contents.url}")
        var pack: String? = null
        runCatching {
            Http.getPlain(contents.downloadUrl!!)?.use { stream ->
                val parseRes = JavaParser().parse(stream)
                parseRes.ifSuccessful { unit ->
                    fun visitRecursive(
                        node: Node,
                        prefix: String,
                    ) {
                        if (node is PackageDeclaration && pack == null) pack = node.nameAsString
                        if (node is ClassOrInterfaceDeclaration) {
                            val kind =
                                when {
                                    node.isInterface -> IndexEntryKind.INTERFACE
                                    node.isAbstract -> IndexEntryKind.ABSTRACT_CLASS
                                    else -> IndexEntryKind.CLASS
                                }
                            found.add(
                                IndexEntry(
                                    "$prefix${node.nameAsString}",
                                    "$pack.$prefix${node.nameAsString}",
                                    "$repository/blob/${contents.path}",
                                    kind,
                                    repository,
                                ),
                            )
                            for (item in node.childNodes) visitRecursive(item, "${node.nameAsString}.")
                        }
                        if (node is EnumDeclaration || node is RecordDeclaration) {
                            val kind = if (node is EnumDeclaration) IndexEntryKind.ENUM else IndexEntryKind.RECORD
                            found.add(
                                IndexEntry(
                                    "$prefix${node.nameAsString}",
                                    "$pack.$prefix${node.nameAsString}",
                                    "$repository/blob/${contents.path}",
                                    kind,
                                    repository,
                                ),
                            )
                            for (item in node.childNodes) visitRecursive(item, "${node.nameAsString}.")
                        }
                        if (node is AnnotationDeclaration) {
                            found.add(
                                IndexEntry(
                                    "$prefix${node.nameAsString}",
                                    "$pack.$prefix${node.nameAsString}",
                                    "https://github.com/$user/$repo/blob/${contents.path}",
                                    IndexEntryKind.ANNOTATION,
                                    repository,
                                ),
                            )
                            for (item in node.childNodes) visitRecursive(item, "${node.nameAsString}.")
                        }
                    }

                    for (node in unit.childNodes) visitRecursive(node, "")
                }
            }
        }.exceptionOrNull()?.printStackTrace()
    }
}
