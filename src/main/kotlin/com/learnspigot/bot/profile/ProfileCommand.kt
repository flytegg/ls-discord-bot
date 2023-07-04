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
    fun onProfileCommand(event: SlashCommandInteractionEvent, @Optional user: User?) {
        val finalUser = user ?: event.user
        val profile = profileRegistry.findByUser(finalUser)

        event.replyEmbeds(
            embed()
                .setTitle("Profile Lookup")
                .addField("Discord", finalUser.asTag + " (" + finalUser.asMention + ")", false)
                .addField("Udemy", profile.udemyProfileUrl ?: "Not linked", false)
                .addField("Reputation", profile.reputation.size.toString(), true)
                .addField("(Notifications)", profile.notifyOnRep.toString(), true)
                .setThumbnail(finalUser.effectiveAvatarUrl)
                .build()
        ).setEphemeral(true).queue()
    }

}