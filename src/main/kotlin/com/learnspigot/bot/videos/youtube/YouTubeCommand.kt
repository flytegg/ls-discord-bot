package com.learnspigot.bot.videos.youtube

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.function.Consumer

class YouTubeCommand {

    @Inject
    private lateinit var youTubeRegistry: YouTubeRegistry

    private val playlistUrl = "https://www.youtube.com/playlist?list=PLAYLIST_ID"

    @Command(
        name = "youtube",
        description = "Search for a video in the YouTube registry"
    )
    fun onYouTubeCommand(
        event: SlashCommandInteractionEvent,
        @Optional @Description("Video title or keywords") query: String?
    ) {
        if (query == null || query.isBlank()) {
            event.reply("Full playlist: $playlistUrl").queue()
            return
        }
        val videos = youTubeRegistry.findVideos(query, 4)
        val topVideo = videos.removeFirst()
        val suggestions = StringBuilder()
        videos.forEach(Consumer { video: YouTubeItem ->
            suggestions.append("- [").append(video.title).append("](").append(video.url()).append(")\n")
        })
        event.replyEmbeds(
            embed()
                .setTitle(topVideo.title, topVideo.url())
                .setDescription((topVideo.description + "\n\n[Click here to watch the video](" + topVideo.url()) + ")")
                .addField("Not quite what you're looking for?", suggestions.toString(), false)
                .build()
        ).queue()
    }
}
