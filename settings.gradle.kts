rootProject.name = "learnspigot-bot"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.7.22"
        id("com.github.johnrengelman.shadow") version "7.1.2"
    }
}

include("db-migrator")