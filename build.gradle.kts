plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.learnspigot"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.flyte.gg/releases")
}

dependencies {
    implementation("gg.flyte:neptune:2.4")
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("org.litote.kmongo:kmongo:5.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("net.dv8tion:JDA:5.0.1") { exclude(module = "opus-java") }
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("com.github.mlgpenguin:MathEvaluator:2.1.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

application {
    mainClass.set("com.learnspigot.bot.MainKt")
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

tasks {
    shadowJar {
        archiveFileName.set("ls-discord-bot.jar")
    }

    jar {
        enabled = false // Disable the standard JAR task
    }

    build {
        dependsOn(shadowJar)
    }

    withType<JavaExec> {
        doFirst {
            val fileName = ".env"
            if (!file(fileName).exists()) return@doFirst
            file(fileName).forEachLine {
                val variable = it.replace("\"", "").split("=", limit = 2)
                if (variable.size > 1) {
                    environment(variable[0], variable[1])
                }
            }
        }
    }
}
