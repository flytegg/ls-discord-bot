plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")
}

group = "com.bot.learnspigot"

allprojects {
    version = "1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("com.github.minndevelopment:jda-ktx:0.9.6-alpha.22")

    implementation("org.jodd:jodd-http:6.3.0")

    implementation("com.google.code.gson:gson:2.10")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.apache.lucene:lucene-analyzers-common:8.11.2")

    implementation("org.spongepowered:configurate-gson:4.1.2")
    implementation("org.spongepowered:configurate-extra-kotlin:4.1.2")

    implementation("dev.morphia.morphia:morphia-core:2.2.10")
    implementation("dev.morphia.morphia:morphia-kotlin:2.2.10")
    implementation(project("db-migrator"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.learnspigot.bot.AppBootstrapKt")
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

    test {
        useJUnitPlatform()
        doFirst {
            val fileName = if(System.getenv("PROD") == "true") ".env.prod" else ".env.dev"
            if(!file(fileName).exists()) return@doFirst
            file(fileName).forEachLine {
                val variable = it.replace("\"", "").split("=", limit = 2)
                if(variable.size > 1) {
                    environment(variable[0], variable[1])
                }
            }
        }
    }

    withType(JavaExec::class) {
        doFirst {
            val fileName = if(System.getenv("PROD") == "true") ".env.prod" else ".env.dev"
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