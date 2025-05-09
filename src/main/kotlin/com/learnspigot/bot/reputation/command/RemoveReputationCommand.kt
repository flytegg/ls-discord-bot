package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RemoveReputationCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Command(
        name = "removerep",
        description = "Remove reputation from a user",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onManageRepRemoveCommand(
        event: SlashCommandInteractionEvent,
        @Description("User to remove reputation from") user: User,
        @Description("ID of the reputation entry to be removed") id: Int,
        @Description("Ending ID range of reputation entries to be removed") @Optional ifRangeEndId: Int?
    ) {
        val profile: Profile = profileRegistry.findByUser(user)
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
