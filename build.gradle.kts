plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.learnspigot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("net.dv8tion:JDA:5.0.0-beta.12") {
        exclude(module = "opus-java")
    }
    implementation("com.github.flytegg:neptune:2.2")
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}

application {
    mainClass.set("com.learnspigot.bot.MainKt")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    build {
        dependsOn(shadowJar)
    }

    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass
        }
    }

    withType(JavaExec::class) {
        doFirst {
            val fileName = ".env"
            if(!file(fileName).exists()) return@doFirst
            file(fileName).forEachLine {
                val variable = it.replace("\"", "").split("=", limit = 2)
                if(variable.size > 1) {
                    environment(variable[0], variable[1])
                }
            }
        }
    }
}
