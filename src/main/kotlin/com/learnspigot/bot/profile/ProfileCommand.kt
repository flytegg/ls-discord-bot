package com.learnspigot.bot.profile

import com.learnspigot.bot.Bot
import com.learnspigot.bot.Registry
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class ProfileCommand {

    @Command("profile")
    @Description("View a user's profile")
    @CommandPermission(Permission.MANAGE_ROLES)
    fun onProfileCommand(
        actor: SlashCommandActor,
        @Description("User to show profile") @Optional user: User?,
        @Description("URL to show profile") @Optional url: String?
    ) {
        val event = actor.commandEvent()

        val profileByURL = Registry.PROFILES.findByURL(url ?: "")

        val embed: MessageEmbed = when {
            user == null && url == null -> userProfileEmbed(event.user)
            user != null && url != null -> createProfileLookupEmbed("Please choose", "Please make a " +
                    "choice whether you want to use a user or use a URL.")
            profileByURL != null -> userProfileEmbed(Bot.jda.getUserById(profileByURL.id)!!)
            user != null -> userProfileEmbed(user)
            else -> createProfileLookupEmbed("Something went wrong", "Something went wrong while " +
                    "trying to find a profile matching your query. Please contact a manager (or higher) to look at " +
                    "this issue.")
        }

        event.replyEmbeds(embed).setEphemeral(true).queue()
    }

    private fun userProfileEmbed(
        user: User
    ): MessageEmbed {
        val profile = Registry.PROFILES.findByUser(user)

        return embed()
            .setTitle("Profile Lookup")
            .addField("Discord", user.name + " (" + user.asMention + ")", false)
            .addField("Udemy", profile.udemyProfileUrl ?: "Not linked", false)
            .addField("Reputation", profile.reputation.size.toString(), true)
            .addField("(Notifications)", profile.notifyOnRep.toString(), true)
            .setThumbnail(user.effectiveAvatarUrl)
            .build()
    }

    private fun createProfileLookupEmbed(
        title: String,
        description: String)
    : MessageEmbed = embed()
        .setTitle("Profile Lookup")
        .addField(title, description, false)
        .build()
}