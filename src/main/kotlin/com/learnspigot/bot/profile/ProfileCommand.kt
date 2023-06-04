package com.learnspigot.bot.profile

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ProfileCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Command(
        name = "profile",
        description = "View a user's profile",
        permissions = [Permission.MANAGE_ROLES]
    )
    fun onProfileCommand(event: SlashCommandInteractionEvent, @Optional user: User = event.user) {
        val profile = profileRegistry.findByUser(user)

        event.replyEmbeds(
            embed()
                .setTitle("Profile Lookup")
                .addField("Discord", user.asTag + " (" + user.asMention + ")", false)
                .addField("Udemy", profile.udemyProfileUrl ?: "Not linked", false)
                .addField("Reputation", profile.reputation.size.toString(), true)
                .addField("(Notifications)", profile.notifyOnRep.toString(), true)
                .setThumbnail(user.effectiveAvatarUrl)
                .build()
        ).setEphemeral(true).queue()
    }

}