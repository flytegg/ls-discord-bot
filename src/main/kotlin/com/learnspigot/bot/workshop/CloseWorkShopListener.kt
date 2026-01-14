package com.learnspigot.bot.workshop

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.util.closeAndLock
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CloseWorkShopListener : ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val channel = event.channel.asThreadChannel()
        if (channel.parentChannel.id != Server.CHANNEL_WORKSHOP.id) return
        if (!event.componentId.endsWith("-close-button") && !event.componentId.startsWith(channel.id)) return

        if (event.member!!.id != channel.ownerId && !event.member.isManager)
            return event.replyEmbeds(embed().setTitle("You cannot close this workshop").build()).setEphemeral(true).queue()


        event.editButton(event.button.asDisabled()).complete()

        Registry.PROFILES.messagesToRemove.remove(channel.id)?.delete()?.queue()
        Registry.WORKSHOP.posts.remove(channel.id)

        Registry.WORKSHOP.channelsMarkedForClosing.add(channel.idLong)
        channel.sendMessageEmbeds(embed().setTitle("Workshop Closed").setDescription("This workshop has been closed.").build()).complete()
        channel.closeAndLock()
    }

}