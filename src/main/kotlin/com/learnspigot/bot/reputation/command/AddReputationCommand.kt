package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class AddReputationCommand {

    @Command("addrep")
    @Description("Add reputation to a user")
    @CommandPermission(Permission.MANAGE_ROLES)
    fun onManageRepAddCommand(
        actor: SlashCommandActor,
        @Description("User to add reputation to") user: User,
        @Description("User who's adding the reputation") @Optional @Named("from-user") fromUser: User?,
        @Description("Channel the reputation is being added from") @Optional @Named("from-channel") fromChannel: TextChannel?,
        @Description("Amount of reputation the user should receive") @Optional amount: Int?
    ) {
        val event = actor.commandEvent()
        val profile = Registry.PROFILES.findByUser(user)
        profile.addReputation(user, fromUser?.id ?: event.user.id, fromChannel?.id ?: event.channel.id, amount ?: 1)
        event.replyEmbeds(
            embed()
                .setTitle("Operation successful")
                .setDescription("Added " + (amount ?: 1) + " reputation to " + user.name + " (" + user.asMention + ")")
                .build()
        ).setEphemeral(true).queue()
    }

}
