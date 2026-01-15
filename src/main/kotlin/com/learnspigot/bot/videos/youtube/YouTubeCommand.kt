package com.learnspigot.bot.videos.youtube

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import java.util.function.Consumer

class YouTubeCommand {

    private val playlistUrl = "https://www.youtube.com/playlist?list=PL3Cdwhu_4crm-taHKMt9PDZ0zQn5U6o-y"

    @Command("youtube")
    @Description("Search for a video in the YouTube registry")
    fun onYouTubeCommand(
        actor: SlashCommandActor,
        @Optional @Description("Video title or keywords") query: String?
    ) {
        val event = actor.commandEvent()
        if (query == null || query.isBlank()) {
            event.reply("Full playlist: $playlistUrl").queue()
            return
        }
        val videos = Registry.YOUTUBE.findVideos(query, 4)
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
