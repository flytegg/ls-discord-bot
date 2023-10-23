package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class PasteCommand {

    @Command(
        name = "pastebin",
        description = "Share the link to the custom pastebin"
    )

    fun onPasteCommand(event: SlashCommandInteractionEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) {
            event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
            return
        }

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != Server.helpChannel.id) {
            return event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
        }

        event.replyEmbeds(getNewPasteBinEmbed()).queue()
    }

    companion object {
        fun getNewPasteBinEmbed() = embed()
            .setTitle("LearnSpigot Pastebin")
            .setDescription("${Server.rightEmoji.asMention} https://paste.learnspigot.com/")
            .addField(
                "How do I use this?",
                "Copy paste your code/error directly from your IDE/console, save it and share the link from the search bar into this chat so we can help.",
                false
            )
            .addField(
                "Important notes:",
                "When sharing code with an error, send the identical class without any changes. Use only one class in each pastebin.",
                true
            )
            .build()
    }
}
