package com.learnspigot.bot.help

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MultiplierCommand {

    @Command(
        name = "multiplier",
        description = "Set a reputation multiplier",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onMultiplierCommand(event: SlashCommandInteractionEvent, multiplier: Int) {
        if (!event.isFromGuild) return
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != Server.helpChannel.id) return

        if (multiplier !in 1..9) {
            event.reply("Multiplier must be 1-9.").setEphemeral(true).queue()
            return
        }

        event.channel.asThreadChannel().getHistoryFromBeginning(1).complete().retrievedHistory[0].apply {
            clearReactions().complete()
            addReaction(Emoji.fromUnicode("${multiplier}\u20E3")).complete()
        }

        event.replyEmbeds(embed()
            .setTitle("${multiplier}x reputation multiplier set")
            .setDescription("Everyone listed as contributor will receive $multiplier reputation once this post is closed.")
            .build()).queue()

        event.jda.getTextChannelById(Environment.get("SUPPORT_CHANNEL_ID"))!!.sendMessageEmbeds(embed()
            .setTitle("${multiplier}x reputation multiplier set")
            .setDescription(channel.asMention)
            .build()).queue()
    }

}