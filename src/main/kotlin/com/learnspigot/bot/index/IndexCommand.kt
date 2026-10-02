package com.learnspigot.bot.index

import com.learnspigot.bot.Registry
import com.learnspigot.bot.index.scraper.GitHubScraper
import com.learnspigot.bot.index.scraper.JavadocScraper
import com.learnspigot.bot.index.scraper.MappingsDevScraper
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class IndexCommand {
    @Command("index repository")
    @Description("Index a GitHub Java repository.")
    @CommandPermission(Permission.ADMINISTRATOR)
    fun onIndexRepo(
        actor: SlashCommandActor,
        @Description("The user/organization that owns the repository.") user: String,
        @Description("The name of the repository.") repo: String,
    ) {
        val event = actor.commandEvent()
        val scraper = GitHubScraper(user, repo)

        scraper.scrape { found ->
            Registry.INDEX.entries.addAll(found.filter { e -> Registry.INDEX.entries.none { it.name == e.name } })
            Registry.INDEX.saveCache(scraper.entrypoint, found)
            sendResultEmbed(event, scraper.entrypoint, found)
        }

        event
            .replyEmbeds(
                embed()
                    .setDescription(
                        "Started a worker thread to index `$user/$repo` on GitHub. You'll receive index results once the indexing is complete.",
                    ).also {
                        if (scraper.canLog) {
                            it.setFooter("\uD83D\uDCA1 Pro Tip: Scraping progress is actively logged during local development")
                        }
                    }.build(),
            ).setEphemeral(true)
            .queue()
    }

    @Command("index javadoc")
    @Description("Index a JavaDoc website from its index page")
    @CommandPermission(Permission.ADMINISTRATOR)
    fun onIndexJavadoc(
        actor: SlashCommandActor,
        @Description("Link to the JavaDoc's index. Choose \"All Classes and Interfaces\" before you copy the link") entrypoint: String,
    ) {
        val event = actor.commandEvent()
        val scraper = JavadocScraper(entrypoint)

        scraper.scrape { found ->
            Registry.INDEX.entries.addAll(found.filter { e -> Registry.INDEX.entries.none { it.name == e.name } })
            Registry.INDEX.saveCache(entrypoint, found)
            sendResultEmbed(event, entrypoint, found)
        }

        event
            .replyEmbeds(
                embed()
                    .setDescription(
                        "Started a worker thread to index `$entrypoint`. You'll receive index results once the indexing is complete.",
                    ).also {
                        if (scraper.canLog) {
                            it
                                .setFooter("\uD83D\uDCA1 Pro Tip: Scraping progress is actively logged during local development")
                        }
                    }.build(),
            ).setEphemeral(true)
            .queue()
    }

    @Command("index mappings")
    @Description("Index NMS mappings from https://mappings.dev/")
    @CommandPermission(Permission.ADMINISTRATOR)
    fun onIndexMappings(
        actor: SlashCommandActor,
        @Description("The Minecraft version for the mappings.") version: String,
        @Description("Choose a version from https://mappings.dev/main.html and paste the link") entrypoint: String,
    ) {
        val event = actor.commandEvent()
        val scraper = MappingsDevScraper(entrypoint)

        scraper.scrape { found ->
            val mapping = Mapping(version, found)
            Registry.INDEX.mappings.add(mapping)
            Registry.INDEX.saveMapping(mapping)
            sendResultEmbed(event, entrypoint, found)
        }

        event
            .replyEmbeds(
                embed()
                    .setDescription(
                        "Started a worker thread to index `$entrypoint`. You'll receive index results once the indexing is complete.",
                    ).also {
                        if (scraper.canLog) {
                            it
                                .setFooter("\uD83D\uDCA1 Pro Tip: Scraping progress is actively logged during local development")
                        }
                    }.build(),
            ).setEphemeral(true)
            .queue()
    }

    private fun sendResultEmbed(
        event: SlashCommandInteractionEvent,
        entrypoint: String,
        found: List<IndexEntry>,
    ) {
        event.user.openPrivateChannel().queue { channel ->
            channel
                ?.sendMessageEmbeds(
                    embed(
                        "Index Results of $entrypoint",
                        """
                            Finished scraping, found ${found.size} entries.
                            * ${IndexEntryKind.CLASS.emoji.formatted} ${found.count {
                            it.kind == IndexEntryKind.CLASS
                        }} classes (+${found.count { it.kind == IndexEntryKind.ABSTRACT_CLASS }} abstract),
                            * ${IndexEntryKind.INTERFACE.emoji.formatted} ${found.count { it.kind == IndexEntryKind.INTERFACE }} interfaces,
                            * ${IndexEntryKind.ENUM.emoji.formatted} ${found.count { it.kind == IndexEntryKind.ENUM }} enums,
                            * ${IndexEntryKind.EVENT.emoji.formatted} ${found.count { it.kind == IndexEntryKind.EVENT }} events,
                            * ${IndexEntryKind.RECORD.emoji.formatted} ${found.count { it.kind == IndexEntryKind.RECORD }} records,
                            * ${IndexEntryKind.UNKNOWN.emoji.formatted} and ${found.count {
                            it.kind == IndexEntryKind.UNKNOWN
                        }} other unresolved entries
                            The index results for this entrypoint have been saved to a BSON file. Everything will be automatically reloaded when the bot launches.
                            
                            You can use `/index invalidate` to delete the cache any time.
                        """.trimIndent(),
                    ),
                )?.queue()
        }
    }

    @Command("index invalidate")
    @Description("Remove the cache file of an indexing entrypoint")
    @CommandPermission(Permission.ADMINISTRATOR)
    fun onIndexInvalidate(
        actor: SlashCommandActor,
        @Optional @Description("The previously used entrypoint") entrypoint: String?,
        @Optional @Description("The previously indexed mapping version") version: String?,
    ) {
        val event = actor.commandEvent()
        if (entrypoint == null && version == null) {
            event
                .reply("Please specify a previously used entrypoint or a mapping version.")
                .setEphemeral(true)
                .queue()
            return
        }

        if (entrypoint != null) {
            if (Registry.INDEX.entries.none { it.entrypoint == entrypoint }) {
                event
                    .reply("There aren't any entries from that entrypoint. Did you put in the right link?")
                    .setEphemeral(true)
                    .queue()
                return
            }
            Registry.INDEX.removeCache(entrypoint)
        }

        if (version != null) {
            if (Registry.INDEX.mappings.none { it.version == version }) {
                event
                    .reply("There aren't any entries from that version. Did you put in the right version?")
                    .setEphemeral(true)
                    .queue()
                return
            }
            Registry.INDEX.removeMappingCache(version)
        }

        event
            .replyFormat("Successfully invalidated the cache.")
            .setEphemeral(true)
            .queue()
    }
}
