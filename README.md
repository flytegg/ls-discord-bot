# LearnSpigot Discord Bot
The discord bot for the [Learnspigot Discord Server](https://learnspigot.com/discord). This powers everything from verification to reputation in help forums and checking the stock of WIX & TSLA.  

The bot is written in Kotlin/JVM, using the Gradle (Kotlin DSL) build tool.
## Libraries
- JDA
- Morphia (Database ORM)
- Jupiter 5 (Unit tests)
- Configurate (File storage)
- Lucene Analyzers (Search scoring)

See `.env.dev` for environement variables. You can put these in `.env.dev` to have the injected into your runtime for the tasks `run` and `test`

## Project setup
1. Copy the `.env.example` into `.env.dev`.
2. Fill in all the environment variables.
   *You can find your udemy client id under your cookies on udemy.com under `client_id` and your udemy bearer under `access_token`*
3. Run `./gradlew run` (`.\gradlew run` on windows) (make sure your using Java 17)
   You can also run `./gradlew test` to run unit tests

## Tasks
```
------------------------------------------------------------
Tasks runnable from root project 'learnspigot-bot'
------------------------------------------------------------

Application tasks
-----------------
run - Runs this project as a JVM application
runShadow - Runs this project as a JVM application using the shadow jar
startShadowScripts - Creates OS specific scripts to run the project as a JVM application using the shadow jar

Build tasks
-----------
assemble - Assembles the outputs of this project.
build - Assembles and tests this project.
buildDependents - Assembles and tests this project and all projects that depend on it.
buildKotlinToolingMetadata - Build metadata json file containing information about the used Kotlin tooling
buildNeeded - Assembles and tests this project and all projects it depends on.
classes - Assembles main classes.
clean - Deletes the build directory.
jar - Assembles a jar archive containing the main classes.
testClasses - Assembles test classes.

Build Setup tasks
-----------------
init - Initializes a new Gradle build.
wrapper - Generates Gradle wrapper files.

Distribution tasks
------------------
assembleDist - Assembles the main distributions
assembleShadowDist - Assembles the shadow distributions
distTar - Bundles the project as a distribution.
distZip - Bundles the project as a distribution.
installDist - Installs the project as a distribution as-is.
installShadowDist - Installs the project as a distribution as-is.
shadowDistTar - Bundles the project as a distribution.
shadowDistZip - Bundles the project as a distribution.

Documentation tasks
-------------------
javadoc - Generates Javadoc API documentation for the main source code.

Help tasks
----------
buildEnvironment - Displays all buildscript dependencies declared in root project 'learnspigot-bot'.
dependencies - Displays all dependencies declared in root project 'learnspigot-bot'.
dependencyInsight - Displays the insight into a specific dependency in root project 'learnspigot-bot'.
help - Displays a help message.
javaToolchains - Displays the detected java toolchains.
kotlinDslAccessorsReport - Prints the Kotlin code for accessing the currently available project extensions and conventions.
outgoingVariants - Displays the outgoing variants of root project 'learnspigot-bot'.
projects - Displays the sub-projects of root project 'learnspigot-bot'.
properties - Displays the properties of root project 'learnspigot-bot'.
tasks - Displays the tasks runnable from root project 'learnspigot-bot' (some of the displayed tasks may belong to subprojects).

Shadow tasks
------------
knows - Do you know who knows?
shadowJar - Create a combined JAR of project and runtime dependencies

Verification tasks
------------------
check - Runs all checks.
test - Runs the test suite.

Rules
-----
Pattern: clean<TaskName>: Cleans the output files of a task.
Pattern: build<ConfigurationName>: Assembles the artifacts of a configuration.
```

*Project is under GNU General Public License v3.0*