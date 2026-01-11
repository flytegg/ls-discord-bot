package com.learnspigot.bot.workshop

import com.learnspigot.bot.Server
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.Button


object WorkShopClose {

    fun closeCommand(event: SlashCommandInteractionEvent, profileRegistry: ProfileRegistry) {

        val channel = event.guildChannel.asThreadChannel()

        if (event.member!!.id != channel.ownerId && !event.member!!.roles.contains(Server.managementRole)) {
            event.replyEmbeds(embed().setTitle("You cannot close this workshop").build()).setEphemeral(true).queue()
            return
        }

        event.deferReply().queue()

        event.hook.sendMessageEmbeds(embed().setTitle("Close confirmation")
            .setDescription("Are you sure you want to close this workshop?").build()
        ).addActionRow(Button.danger(channel.id + "-close-button", "Close"))
            .queue { message: Message -> profileRegistry.messagesToRemove[event.channel.id] = message }


    }

}