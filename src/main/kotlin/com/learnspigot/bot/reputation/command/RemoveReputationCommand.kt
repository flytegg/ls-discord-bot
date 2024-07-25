package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.database.profile.removeReputation
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.profile
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RemoveReputationCommand {

    @Command(
        name = "removerep",
        description = "Add reputation to a user",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onManageRepRemoveCommand(
        event: SlashCommandInteractionEvent,
        @Description("User to remove reputation from") user: User,
        @Description("ID of the reputation entry to be removed") id: Int,
        @Description("Ending ID range of reputation entries to be removed") @Optional ifRangeEndId: Int?
    ) {
        val profile = user.profile
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