package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class AddReputationCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Command(
        name = "addrep",
        description = "Add reputation to a user",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onManageRepAddCommand(event: SlashCommandInteractionEvent, user: User, fromUser: User, fromChannel: Channel, @Optional amount: Int = 1) {
        val profile = profileRegistry.findByUser(user)
        profile.addReputation(user, fromUser.id, fromChannel.id, amount)
        event.replyEmbeds(
            embed()
                .setTitle("Operation successful")
                .setDescription("Added " + amount + " reputation to " + user.asTag + " (" + user.asMention + ")")
                .build()
        ).setEphemeral(true).queue()
    }

}