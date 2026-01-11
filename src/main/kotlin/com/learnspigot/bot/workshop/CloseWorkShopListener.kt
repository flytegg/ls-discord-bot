package com.learnspigot.bot.workshop

import com.learnspigot.bot.Server
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CloseWorkShopListener : ListenerAdapter() {

    @Inject
    lateinit var profileRegistry: ProfileRegistry

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val channel = event.channel.asThreadChannel()
        if (channel.parentChannel.id != Server.workshopChannel.id) return
        if (!event.componentId.endsWith("-close-button") && !event.componentId.startsWith(channel.id)) return

        event.editButton(event.button.asDisabled()).complete()


        profileRegistry.messagesToRemove[channel.id]?.delete()?.queue()
        channel.sendMessageEmbeds(embed().setTitle("Workshop Closed").setDescription("This workshop has been close.").build()).complete()

        channel.manager.setArchived(true).setLocked(true).complete()

    }

}