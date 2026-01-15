package com.learnspigot.bot.embed

import com.learnspigot.bot.Bot
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class EmbedCommand {

    @Command("embed")
    @Description("Create an embed message")
    @CommandPermission(Permission.MANAGE_PERMISSIONS)
    fun onEmbedCommand(
        actor: SlashCommandActor,
        @Description("Channel to send embed") channel: TextChannel,
        @Description("Embed title") title: String,
        @Description("Embed description") description: String,
        @Description("Embed footer") @Optional footer: String?,
        @Description("Embed thumbnail") @Optional thumbnail: String?,
        @Description("Embed image") @Optional image: String?,
        @Description("Embed author") @Optional author: String?,
        @Description("Embed color") @Optional color: Int?,
    ) {
        val event = actor.commandEvent()
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
