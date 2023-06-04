package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.profile.Profile
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
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
        description = "Add reputation to a user",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onManageRepRemoveCommand(event: SlashCommandInteractionEvent, user: User, id: Int, @Optional ifRangeEndId: Int = id) {
        val profile: Profile = profileRegistry.findByUser(user)
        profile.removeReputation(id, ifRangeEndId)
        val repRemoveOutput =
            (if (id == ifRangeEndId) "Removed reputation with ID $id" else "Removed reputation within ID range $id - $ifRangeEndId") + " from " + user.asTag + " (" + user.asMention + ")"
        event.interaction.replyEmbeds(
            embed()
                .setTitle("Operation successful")
                .setDescription(repRemoveOutput)
                .build()
        ).setEphemeral(true).queue()
    }

}