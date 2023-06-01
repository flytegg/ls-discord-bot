package com.learnspigot.bot.help

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class PasteCommand {

    @Command(
        name = "pastebin",
        description = "Share the link to learnspigot pastebin"
    )

    fun onPasteCommand(event: SlashCommandInteractionEvent){
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD){
            event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
            return
        }

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) {
            event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
            return
        }

        event.replyEmbeds(
            embed()
                .setTitle("LearnSpigot powered Pastebin")
                .setDescription("Visit: https://paste.learnspigot.com/")
                .addField(
                    "How do I use this?",
                    "Copy paste code/error directly from your IDE/Console, save it and share the link from the search bar",
                    false
                )
                .addField(
                    "Important notes: ",
                    "When sharing code with an error, send the identical class without any changes. Use only one class in each pastebin.",
                    true
                )
                .build())
            .queue()
    }
}