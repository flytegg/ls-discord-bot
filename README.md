![Discord Banner 2](https://discordapp.com/api/guilds/397526357191557121/widget.png?style=banner2)

# LearnSpigot Discord Bot
This is the Discord bot for the [LearnSpigot Discord Server](https://learnspigot.com/discord). LearnSpigot is the most sold Minecraft course in the world, and this bot aids the provision of exclusive support for all students in the server.

This bot powers systems such as verification, tickets and suggestions.

## Technologies
Written in Kotlin/JVM. Using Gradle (Kotlin DSL) build tool.

## Libraries
- [JDA](https://github.com/DV8FromTheWorld/JDA) (Java Discord API)
- [MongoDB Java Driver](https://github.com/mongodb/mongo-java-drive) (Database)
- [Neptune](https://github.com/flytegg/neptune/) (Command framework)

## Contributing

Contributions are always welcome. If you have no coding knowledge, please create an issue in the Issues tab so we can track it. Otherwise, please use the following steps to begin contributing to the code:

1. Fork the repository, and then clone it to your local git
2. Open the project in your IDE of choice
3. We use environment variables for sensitive data such as Mongo URI's and bot tokens, as well as constants such as channel IDs or role IDs. You will see an .env.example in the root folder. You should rename this to .env, and populate it. We recommend using [this plugin](https://plugins.jetbrains.com/plugin/7861-envfile) to easily bind the env file to your project so they are available for testing locally
4. Make your changes, and please maintain a similar code style and quality
5. Create a Pull Request into the master branch of this repository


We review pull requests as soon as possible. Please feel free to get in touch if it's urgent.

If you are an active contributor or close to the [Flyte](https://flyte.gg) team, you may be offered access to the official LearnSpigot bot testing server where preconfigured .env files are provided with bot tokens and a database. Otherwise, all the tools are provided to work locally.
