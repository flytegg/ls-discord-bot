package com.learnspigot.bot.help

import com.learnspigot.bot.Server
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
        if (channel.parentChannel.id != Server.CHANNEL_HELP.id) {
            event.reply("This can only be used in a help thread!").setEphemeral(true).queue()
            return
        }

        val closeId = event.guild!!.retrieveCommands().complete()
            .firstOrNull { it.name == "close" }
            ?.id

        event.replyEmbeds(
            embed()
                .setTitle("Are there any updates?")
                .setDescription(" ")
                .addField(
                    "I have new code/error",
                    "Paste it @ https://paste.learnspigot.com and send it so we can help.",
                    false
                )
                .addField("I figured it out", "Great job! Run ${if (closeId == null) "/close" else "</close:$closeId>"} and select contributors.", false)
                .build())
            .setContent(channel.owner!!.asMention + " - You haven't responded in a while!")
            .queue()
    }

}