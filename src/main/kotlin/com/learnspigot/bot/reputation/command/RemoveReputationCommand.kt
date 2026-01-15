package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.Registry
import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class RemoveReputationCommand {

    @Command(
        "removerep"
    )
    @Description("Remove reputation from a user")
    @CommandPermission(Permission.MANAGE_ROLES)
    fun onManageRepRemoveCommand(
        actor: SlashCommandActor,
        @Description("User to remove reputation from") user: User,
        @Description("ID of the reputation entry to be removed") id: Int,
        @Description("Ending ID range of reputation entries to be removed") @Optional @Named("if-range-end-id") ifRangeEndId: Int?
    ) {
        val event = actor.commandEvent()
        val profile: Profile = Registry.PROFILES.findByUser(user)
        profile.removeReputation(id, ifRangeEndId ?: id)
        val repRemoveOutput =
            (if (id == ifRangeEndId) "Removed reputation with ID $id" else "Removed reputation within ID range $id - $ifRangeEndId") + " from " + user.name + " (" + user.asMention + ")"
        event.interaction.replyEmbeds(
            embed()
                .setTitle("Operation successful")
                .setDescription(repRemoveOutput)
                .build()
        ).setEphemeral(true).queue()
    }

}
