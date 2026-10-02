package com.learnspigot.bot.reference

import com.learnspigot.bot.Registry
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class MappingProvider {
    fun getSuggestions(version: String?): Collection<String> =
        Registry.INDEX.mappings
            .map { it.version }
            .filter { it.startsWith(version ?: "") }
            .take(OptionData.MAX_CHOICES)
}
