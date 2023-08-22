package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
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
    fun onManageRepAddCommand(
        event: SlashCommandInteractionEvent,
        @Description("User to add reputation to") user: User,
        @Description("User who is adding the reputation") @Optional fromUser: User?,
        @Description("Channel the reputation is being added from") @Optional fromChannel: Channel?,
        @Description("Amount of reputation the user should receive") @Optional amount: Int?
    ) {
        val profile = profileRegistry.findByUser(user)
        profile.addReputation(user, fromUser?.id ?: event.user.id, fromChannel?.id ?: event.channel.id, amount ?: 1)
        event.replyEmbeds(
            embed()
                .setTitle("Operation successful")
                .setDescription("Added " + (amount ?: 1) + " reputation to " + user.name + " (" + user.asMention + ")")
                .build()
        ).setEphemeral(true).queue()
    }

}