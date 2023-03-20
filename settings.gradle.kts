rootProject.name = "learnspigot-bot"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.0"
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

include("db-migrator")
