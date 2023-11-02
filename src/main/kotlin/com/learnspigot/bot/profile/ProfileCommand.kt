package com.learnspigot.bot.profile

import com.learnspigot.bot.Bot
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
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
    fun onProfileCommand(
        event: SlashCommandInteractionEvent,
        @Description("User to show profile") @Optional user: User?,
        @Description("URL to show profile") @Optional url: String?
    ) {
        val profileByURL = profileRegistry.findByURL(url ?: "")

        if (user != null && url != null) {
            event.replyEmbeds(
                embed()
                    .setTitle("Profile Lookup")
                    .addField(
                        "Please choose",
                        "Please make a choice whether you want to use a user or use a URL.",
                        false)
                    .build()
            ).setEphemeral(true).queue()
            return
        }

        if (user == null && url == null) {
            showUserProfile(event, event.user)
            return
        }

        if (profileByURL != null) {
            val urlUser = Bot.jda.getUserById(profileByURL.id)

            if (urlUser == null) {
                event.replyEmbeds(
                    embed()
                        .setTitle("Profile Lookup")
                        .addField(
                            "No discord user found",
                            "No discord user was found using this udemy URL. This is an issue on our end, " +
                                    "please contact a manager (or higher) to solve your issue.",
                            false)
                        .build()
                ).setEphemeral(true).queue()
                return
            }

            showUserProfile(event, urlUser)
            return
        }

        if (user != null) {
            showUserProfile(event, user)
            return
        }

        if (profileByURL == null) {
            event.replyEmbeds(
                embed()
                    .setTitle("Profile Lookup")
                    .addField(
                        "Invalid URL",
                        "An invalid udemy URL has been provided, so no profile could be found.",
                        false)
                    .build()
            ).setEphemeral(true).queue()
            return
        }
    }

    private fun showUserProfile(
        event: SlashCommandInteractionEvent,
        user: User
    ) {
        val profile = profileRegistry.findByUser(user)

        event.replyEmbeds(
            embed()
                .setTitle("Profile Lookup")
                .addField("Discord", user.name + " (" + user.asMention + ")", false)
                .addField("Udemy", profile.udemyProfileUrl ?: "Not linked", false)
                .addField("Reputation", profile.reputation.size.toString(), true)
                .addField("(Notifications)", profile.notifyOnRep.toString(), true)
                .setThumbnail(user.effectiveAvatarUrl)
                .build()
        ).setEphemeral(true).queue()
    }
}