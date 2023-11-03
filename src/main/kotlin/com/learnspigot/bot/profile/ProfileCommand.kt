package com.learnspigot.bot.profile

import com.learnspigot.bot.Bot
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
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

        val embed: MessageEmbed = when {
            user == null && url == null -> {
                userProfileEmbed(event.user)
            }
            user != null && url != null -> {
                embed()
                    .setTitle("Profile Lookup")
                    .addField(
                        "Please choose",
                        "Please make a choice whether you want to use a user or use a URL.",
                        false)
                    .build()
            }
            profileByURL != null -> {
                val urlUser = Bot.jda.getUserById(profileByURL.id)
                userProfileEmbed(urlUser!!)
            }
            user != null -> {
                userProfileEmbed(user)
            }
            else -> {
                embed()
                    .setTitle("Profile Lookup")
                    .addField(
                        "Something went wrong",
                        "Something went wrong while trying to find a profile matching your query. Please " +
                                "contact a manager (or higher) to look at this issue.",
                        false)
                    .build()
            }
        }

        event.replyEmbeds(embed).setEphemeral(true).queue()
    }

    private fun userProfileEmbed(
        user: User
    ): MessageEmbed {
        val profile = profileRegistry.findByUser(user)

        return embed()
            .setTitle("Profile Lookup")
            .addField("Discord", user.name + " (" + user.asMention + ")", false)
            .addField("Udemy", profile.udemyProfileUrl ?: "Not linked", false)
            .addField("Reputation", profile.reputation.size.toString(), true)
            .addField("(Notifications)", profile.notifyOnRep.toString(), true)
            .setThumbnail(user.effectiveAvatarUrl)
            .build()
    }
}