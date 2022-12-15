plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

group = "com.learnspigot.bot.db-migrator"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.8.1")

    implementation("com.google.code.gson:gson:2.10")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation(kotlin("test"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
        doFirst {
            val fileName = if(System.getenv("PROD") == "true") ".env.prod" else ".env.dev"
            if(!rootProject.file(fileName).exists()) return@doFirst
            rootProject.file(fileName).forEachLine {
                val variable = it.replace("\"", "").split("=", limit = 2)
                if(variable.size > 1) {
                    environment(variable[0], variable[1])
                }
            }
        }
    }
}