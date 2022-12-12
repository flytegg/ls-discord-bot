package com.learnspigot.bot.manager

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KProperty

object FileManager {

    private val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
        .build()

    inline fun <reified T : Any> loadConfig(name: String): T {
        val loader = retrieveLoaderJson(name)

        val node = loader.load()
        val config = node.get(T::class.java)!!
        saveConfig(name, config)
        return config
    }

    inline operator fun <reified T : Any> getValue(owner: Any, property: KProperty<*>): T = loadConfig("${property.name}.json")

    fun <T : Any> saveConfig(name: String, configClass: T) {
        val loader = retrieveLoaderJson(name)

        val node: ConfigurationNode = loader.load()

        node.set(configClass::class.java, configClass)
        loader.save(node)
    }


    fun retrieveLoaderJson(name: String): GsonConfigurationLoader = GsonConfigurationLoader.builder()
        .also { if(!File(System.getProperty("user.dir")).exists()) File(System.getProperty("user.dir")).mkdirs() }
        .path(Path.of(System.getProperty("user.dir"), name))
        .defaultOptions {
            it.shouldCopyDefaults(true)
            it.serializers { builder ->
                builder.registerAll(serializers)
            }
        }
        .indent(4)
        .build()
}