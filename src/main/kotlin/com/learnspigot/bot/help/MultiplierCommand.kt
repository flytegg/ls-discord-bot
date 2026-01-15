package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class MultiplierCommand {

    @Command("multiplier")
    @Description("Set a reputation multiplier")
    @CommandPermission(Permission.MANAGE_ROLES)
    fun onMultiplierCommand(actor: SlashCommandActor, multiplier: Int) {
        val event = actor.commandEvent()
        if (!event.isFromGuild) return
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != Server.CHANNEL_HELP.id) return

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

        Server.CHANNEL_SUPPORT.sendMessageEmbeds(embed()
            .setTitle("${multiplier}x reputation multiplier set")
            .setDescription(channel.asMention)
            .build()).queue()
    }

}