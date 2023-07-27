package com.learnspigot.bot.embed

import com.learnspigot.bot.Bot
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class EmbedCommand {
    @Command(
        name = "embed",
        description = "Create an embed message"
    )
    fun onEmbedCommand(
        event: SlashCommandInteractionEvent,
        @Description("Channel to send embed") channel: Channel,
        @Description("Embed title") title: String,
        @Description("Embed description") description: String,
        @Description("Embed footer") @Optional footer: String?,
        @Description("Embed thumbnail") @Optional thumbnail: String?,
        @Description("Embed image") @Optional image: String?,
        @Description("Embed author") @Optional author: String?,
        @Description("Embed color") @Optional color: Int?,
    ) {
        event.replyEmbeds(
            embed()
                .setTitle("Successfully created embed")
                .setDescription("The embed has been sent in ${channel.asMention}.")
                .build()
        ).setEphemeral(true).queue()

        Bot.jda.getTextChannelById(channel.id)?.sendMessageEmbeds(
            embed()
                .setTitle(title)
                .setDescription(description.replace("\\n", "\n"))
                .setFooter(footer)
                .setThumbnail(thumbnail)
                .setImage(image)
                .setAuthor(author)
                .setColor(color ?: 0x2B2D31)
                .build()
        )?.queue()
    }
}