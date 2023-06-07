package com.learnspigot.bot.help

import com.learnspigot.bot.Environment
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CloseListener : ListenerAdapter() {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.componentId != event.channel.id + "-contributor-selector") return
        val channel = event.channel.asThreadChannel()

        if (event.member!!.id != channel.ownerId && !event.member!!.roles.contains(event.guild!!.getRoleById(Environment.get("MANAGEMENT_ROLE_ID")))) {
            event.reply("You cannot close this thread!").setEphemeral(true).queue()
            return
        }

        event.interaction.deferEdit().queue()

        profileRegistry.contributorSelectorCache[event.channel.id] = event.values
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!event.componentId.endsWith("-close-button")) return
        val channel = event.channel.asThreadChannel()

        if (event.member!!.id != channel.ownerId && !event.member!!.roles.contains(event.guild!!.getRoleById(Environment.get("MANAGEMENT_ROLE_ID")))) {
            event.reply("You cannot close this thread!").setEphemeral(true).queue()
            return
        }

        event.editButton(event.button.asDisabled()).complete()

        val contributors = profileRegistry.contributorSelectorCache[event.channel.id] ?: mutableListOf()

        contributors.map { event.guild!!.retrieveMemberById(it).complete().user }.forEach { profileRegistry.findByUser(it).addReputation(it, channel.ownerId, channel.id, 1) }

        profileRegistry.messagesToRemove[channel.id]?.delete()?.queue()

        event.channel.sendMessageEmbeds(embed()
            .setTitle(event.member!!.effectiveName + " has closed the thread")
            .setDescription("Listing ${if (contributors.isEmpty()) "no contributors." else contributors.joinToString(", ") { "<@$it>" } + " as contributors."}")
            .build()).complete()

        channel.manager.setArchived(true).setLocked(true).complete()
    }

}