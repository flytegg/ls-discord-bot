package com.learnspigot.bot.index

import com.learnspigot.bot.Bot
import okio.ByteString.Companion.decodeBase64
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import kotlin.io.path.*

class IndexRegistry {
    val entries: MutableList<IndexEntry> = mutableListOf()
    val mappings: MutableList<Mapping> = mutableListOf()
    val base = Bot.fromEnv("INDEX_CACHE_DIRECTORY")

    private fun String.encodeBase64() = String(Base64.getEncoder().encode(toByteArray()))

    init {
        loadAllCache()
    }

    private fun loadAllCache() {
        val path = Path(Bot.fromEnv("INDEX_CACHE_DIRECTORY"))
        if (!path.exists() || !path.isDirectory()) {
            System.err.println("[INDEX] Cache directory could not be found, not loading any cache")
            return
        }

        for(file in path.listDirectoryEntries("*.index")) {
            val indexFile = IndexFile(emptyArray())
            val entrypoint =
                file.name
                    .substringBeforeLast('.')
                    .decodeBase64()
                    .toString()
            DataInputStream(file.inputStream()).use { indexFile.read(it,entrypoint) }
            entries.addAll(indexFile.entries.filter { et -> entries.none { et.name == it.name } })
        }

        val mappingsPath = Path(path.toString(), "mapping")
        if (mappingsPath.exists() && mappingsPath.isDirectory()) {
            for (file in mappingsPath.listDirectoryEntries("*.index")) {
                val fileNameExtension = file.fileName.toString()
                val version = fileNameExtension.substringBeforeLast('.')
                val indexFile = IndexFile(emptyArray())
                DataInputStream(file.inputStream()).use { indexFile.read(it,version); }
                mappings.add(Mapping(version, indexFile.entries.toList()))
            }
        }
    }

    fun saveCache(
        entrypoint: String,
        entries: List<IndexEntry>,
    ) {
        Path(base, "${entrypoint.encodeBase64()}.index")
            .also { if (!it.exists()) it.createFile() }
            .outputStream()
            .let { DataOutputStream(it) }
            .use {
                IndexFile(entries.toTypedArray()).write(it)
                it.flush()
            }
    }

    fun removeCache(entrypoint: String) {
        Path(base, "${entrypoint.encodeBase64()}.index").deleteIfExists()
        entries.removeIf { it.entrypoint == entrypoint }
    }

    fun removeMappingCache(version: String) {
        Path(base, "mapping", "$version.index").deleteIfExists()
        mappings.removeIf { it.version == version }
    }

    fun saveMapping(mapping: Mapping) {
        Path(base, "mapping", "${mapping.version}.index")
            .also {
                if (!it.exists()) {
                    it.parent.createDirectories()
                    it.createFile()
                }
            }
            .outputStream()
            .let { DataOutputStream(it) }
            .use {
                IndexFile(entries.toTypedArray()).write(it)
                it.flush()
            }
    }
}
