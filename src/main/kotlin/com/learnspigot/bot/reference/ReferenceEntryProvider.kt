package com.learnspigot.bot.reference

import com.learnspigot.bot.Registry
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.regex.Pattern

class ReferenceEntryProvider {
    val suggestionCache: MutableMap<String, List<Command.Choice>> = mutableMapOf()

    fun suggest(
        inp: String?,
        mappingVersion: String?,
    ): List<Command.Choice> {
        val input = inp ?: ""
        val inputTerms = terms(input)
        val cacheKey = "$mappingVersion:${inputTerms.joinToString(",")}"

        if (cacheKey in suggestionCache) return suggestionCache[cacheKey] ?: emptyList()

        val mapping = Registry.INDEX.mappings.find { it.version == mappingVersion }
        if (mappingVersion != null && mapping == null) return emptyList()

        val sourceEntries = mapping?.entries ?: Registry.INDEX.entries
        val res =
            sourceEntries
                .asSequence()
                .withIndex()
                .filter { (_, entry) -> inputTerms.all { entry.name.contains(it, ignoreCase = true) } }
                .sortedByDescending { (_, entry) ->
                    inputTerms.count { entry.name.contains(it, ignoreCase = true) } +
                        (if (entry.simpleName == input) 1 else 0) +
                        (if (entry.simpleName.contains(input, ignoreCase = true)) 1 else 0)
                }.take(OptionData.MAX_CHOICES)
                .map { (index, entry) ->
                    Command.Choice(
                        "${entry.kind.nativeEmoji.formatted} ${entry.simpleName} (${entry.name.tryShortenPackage()})".shortenTo100(),
                        index.toLong(),
                    )
                }.toList()

        suggestionCache[cacheKey] = res
        return res
    }

    private fun terms(input: String): List<String> {
        val set = mutableSetOf<String>()
        for (str in input.split(Regex("\\s+"))) {
            if (str.lowercase() == str) {
                set.add(str)
                continue
            }
            val matcher = Pattern.compile("[A-Z]?[a-z]+|[A-Z]+(?=[A-Z][a-z]|$)").matcher(str)
            while (matcher.find()) {
                set.add(matcher.group())
            }
        }
        return set.sorted()
    }

    // most common and recognized packages only
    private fun String.tryShortenPackage() =
        this
            .replace("net.minecraft.server", "NMS")
            .replace("net.minecraft", "MC")
            .replace("org.bukkit", "BKT")
            .replace("com.destroystokyo.paper", "PPR")
            .replace("io.papermc.paper", "PPR")
            .replace("org.spigotmc", "SPT")

    private fun String.shortenTo100() = if (length <= 100) this else "${substring(0, 97)}..."
}
