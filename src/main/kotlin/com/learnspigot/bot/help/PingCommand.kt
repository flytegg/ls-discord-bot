package com.learnspigot.bot.help

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class PingCommand {

    @Command(
        name = "ping",
        description = "Remind a student who has abandoned their ticket",
        permissions = [Permission.MANAGE_EMOJIS_AND_STICKERS]
    )
    fun onPingCommand(event: SlashCommandInteractionEvent) {
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) {
            event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
            return
        }

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) {
            event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
            return
        }

        var closeId = ""
        for (command in event.guild!!.retrieveCommands().complete()) {
            if (command.name == "close") {
                closeId = command.id
            }
        }

        event.replyEmbeds(
            embed()
                .setTitle("Are there any updates?")
                .setDescription(" ")
                .addField(
                    "I have new code/error",
                    "Paste it @ https://paste.learnspigot.com and send it so we can help.",
                    false
                )
                .addField("I figured it out", "Great job! Run </close:$closeId> and select contributors.", false)
                .build())
            .setContent(channel.owner!!.asMention + " - You haven't responded in a while!")
            .queue()
    }

}