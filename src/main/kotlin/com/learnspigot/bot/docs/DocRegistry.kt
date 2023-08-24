package com.learnspigot.bot.docs

import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*

// Code written by Josh! (Only imported by Stephen)
// Sidenote: If the Javadocs structure changes, this whole system will go up in flames
class DocRegistry {

    private val baseURL = "https://hub.spigotmc.org/javadocs/bukkit"

    private var index: Map<String, String> = TreeMap(String.CASE_INSENSITIVE_ORDER)

    init {
        populateIndexFromMongo()
    }

    fun search(query: String): QueryResult? {
        index[query]?.let {
            println("Found $it for search $query")
            val result = query(it, if ("#" in query) query.split("#")[1] else null)
            println(result)
            return result
        }
        println("No result found")
        return null
    }

    private fun query(path: String, id: String?): QueryResult {
        val connection = URL("$baseURL/$path").openConnection()

        BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
            val content = reader.readText()

            val split = content.split("<div class=\"type-signature\"><span class=\"modifiers\">")[1].replace(
                "</span><span class=\"element-name type-name-label\">",
                ""
            ).split("</span>")

            val classTitle = split[0]

            val split2 = split[1].replace("<span class=\"extends-implements\">", "")

            val classExtends = if ("enum" in classTitle || "extends" !in split2) null else
                split2.split(if (split2.contains("implements ")) "implements " else "</span></div>")[0].split(", ").map {
                    val split3 = it.split("<a href=\"")[1].split("\" title=\"")
                    val name = split3[1].split("\">")[1].split("</a>")[0]
                    return@map "[$name]($baseURL/${index[name]})"
                }

            var classImplements: List<String>? = null

            if (split2.contains("implements ")) {
                classImplements = split2.split("implements ")[1].split("</span></div>")[0].split(", ").map {
                    val split3 = it.split("<a href=\"")[1].split("\" title=\"")
                    val name = split3[1].split("\">")[1].split("</a>")[0]
                    return@map "[$name]($baseURL/${index[name]})"
                }
            }

            if (id == null) {
                val classDescription = content.split("</span></div>")[1].replace("<div class=\"block\">", "").substring(1)
                    .split("</div>")[0].replace("\n", " ").replace(" <p> ", "\n\n").split(" <a ").map {
                    val part =
                        it.replace("href=\"", "").replace("../", "").replace(".html", "").replace("(", "").replace(")", "")
                            .split("\"><code>")

                    if (part.size < 2) return@map part[0]

                    return@map "[${part[0]}]($baseURL/${index[part[0]]})" + part[1].split("</code></a>")[1]
                }.joinToString(" ")

                return ClassQueryResult(
                    classTitle,
                    classExtends ?: listOf(),
                    classImplements ?: listOf(),
                    classDescription
                )
            }

            val method = content.split("<section class=\"detail\" id=\"${path.split("#")[1]}\">")[1].split("</section>")[0]

            var methodTitle =
                method.split("<div class=\"member-signature\">")[1].split("</div>")[0]
                    .replace("</span>", "")
                    .replace("<span class=\"modifiers\">", "")
                    .replace("</span>", "")
                    .replace("</span><wbr><span class=\"parameters\">", "")
                    .replace("&nbsp;", " ")

            var methodDescription: String? = null

            val split3 = path.substring(path.lastIndexOf("/") + 1).split("#")

            if ("enum" in methodTitle) {
                methodTitle =
                    methodTitle.split("<span class=\"return-type\">")[0] + "${split3[0].replace(".html", "")} ${split3[1]}"
            } else {
                methodTitle =
                    methodTitle.replace("<span class=\"return-type\">", "").replace("<span class=\"element-name\">", "")
                        .replace("<wbr><span class=\"parameters\">", "")
                val description = method.split("<div class=\"block\">")[1].split("</div>")[0].split("<a href=\"")
                if (description.size > 1) {
                    val desc2 = description[1].split("\"><code>")
                    methodDescription = description[0] + "[${
                        desc2[0].replace(
                            "#",
                            ""
                        )
                    }]($baseURL/${path.split("#")[0]}${desc2[0]})" + desc2[1].split("</code></a>")[1]
                } else {
                    methodDescription = description[0]
                }
            }

            var methodReturn: String? = null

            if (method.contains("<dt>Returns:</dt>")) {
                methodReturn =
                    method.split("<dt>Returns:</dt>\n")[1].split("\n</dl>")[0].replace("<dd>", "").replace("</dd>", "")
            }

            val methodParameters = mutableMapOf<String, String>()

            if (method.contains("<dt>Parameters:</dt>")) {
                var stop = false
                method.split("<dt>Parameters:</dt>\n")[1].split("\n</dl>")[0].split("<dd>").forEach {
                    if (stop) return@forEach
                    val split4 = it.split(" - ")
                    if (split4.size > 1) methodParameters += split4[0].replace("<code>", "")
                        .replace("</code>", "") to split4[1].split("</dd>")[0]
                    if (it.contains("<dt>Throws:</dt>")) {
                        stop = true
                        return@forEach
                    }
                }
            }

            val methodThrows = mutableMapOf<String, String>()

            if (method.contains("<dt>Throws:</dt>")) {
                method.split("<dt>Throws:</dt>\n")[1].split("\n</dl>")[0].split("<dd>").forEach {
                    val split4 = it.split(" - ")
                    if (split4.size > 1) {
                        val link = split4[1].split("</dd>")[0].replace("\n", " ").split("<a href=\"")
                        methodThrows += split4[0].replace("<code>", "")
                            .replace("</code>", "")
                            .split("\">")[1].split("</a>")[0] to "${link[0]}[${link[1].split("\"><code>")[0].replace("#", "")}]($baseURL/${path.split("#")[0]}${link[1].split("\"><code>")[0]})"
                    }
                }
            }


            return MethodQueryResult(
                classTitle,
                classExtends ?: listOf(),
                classImplements ?: listOf(),
                methodTitle,
                methodDescription,
                methodReturn,
                methodParameters,
                methodThrows
            )
        }
    }

    data class ClassQueryResult(
        override val classTitle: String,
        override val classExtends: List<String>,
        override val classImplements: List<String>,
        val description: String
    ) : QueryResult(classTitle, classExtends, classImplements)

    data class MethodQueryResult(
        override val classTitle: String,
        override val classExtends: List<String>,
        override val classImplements: List<String>,
        val methodTitle: String,
        val methodDescription: String?,
        val methodReturn: String?,
        val methodParameters: Map<String, String>,
        val methodThrows: Map<String, String>,
    ) : QueryResult(classTitle, classExtends, classImplements)

    open class QueryResult(
        open val classTitle: String,
        open val classExtends: List<String>,
        open val classImplements: List<String>
    )

    private fun populateIndexFromMongo() {
        val start = Instant.now()

        Mongo.docsCollection.find().first()?.let {
            index = it["index"] as? Map<String, String> ?: emptyMap()
        }

        println("Indexed ${index.size} entries from MongoDB in ${Duration.between(start, Instant.now()).seconds} seconds")
    }

    private fun populateIndex() {
        val connection = URL("$baseURL/index-all.html").openConnection()

        val start = Instant.now()

        BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
            reader.readLines().forEach {
                if (!(it.startsWith("<dt><a href=\"") && it.endsWith("</a></dt>"))) return@forEach
                val split = it.split("<dt><a href=\"")[1].split("\" class=\"")
                val split2 = split[1].split("\">")[1].split("</a>")

                val link = split[0]
                var name = split2[0]
                val description = split2[1]

                if (description.lowercase().contains("in class") || description.lowercase()
                        .contains("in interface") || description.lowercase().contains("in enum")
                ) {
                    // It's part of a class so we should turn the name/key into Class#thing
                    name = link.substring(link.lastIndexOf("/") + 1).replace(".html", "").split("(")[0]
                }

                index += name to link

                println("$name ($link)")

                if (split2.size > 1) {
                    if (description.lowercase().contains("method in interface")) {
                        val interfaceLink = description.split("<a href=\"")[1].split("\" title=\"interface in")[0]

                        getAllKnownSubinterfaces(interfaceLink).forEach { subinterface ->
                            val extraName = subinterface.substring(subinterface.lastIndexOf("/") + 1)
                                .replace(".html", "") + "#${name.split("#")[1]}"
                            println("INTERFACE DUPLICATE $extraName ($link)")
                            index += extraName to link
                        }
                    }
                }
            }
        }

        println("Finished indexing in ${Duration.between(start, Instant.now()).seconds} seconds")

        Document().apply {
            this["version"] = "1.20.1"
            this["index"] = index
        }.also {
            Mongo.docsCollection.replaceOne(Filters.eq("version", "1.20.1"), it, ReplaceOptions().upsert(true))
        }
    }

    private fun getAllKnownSubinterfaces(interfaceLink: String): List<String> {
        val connection = URL("$baseURL/$interfaceLink").openConnection()

        val links = mutableListOf<String>()

        BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
            var inAllKnownSubinterfaces = false

            reader.readLines().forEach { line ->
                if (line == "<dt>All Known Subinterfaces:</dt>") {
                    inAllKnownSubinterfaces = true
                    return@forEach
                }

                if (!inAllKnownSubinterfaces) return@forEach

                line.split(", ").forEach {
                    links += getSubinterfaceLinkFromString(it)
                }

                return links
            }
        }

        return links
    }

    private fun getSubinterfaceLinkFromString(string: String) =
        string.split("<a href=\"")[1].split("\" title=\"interface in ")[0]

}