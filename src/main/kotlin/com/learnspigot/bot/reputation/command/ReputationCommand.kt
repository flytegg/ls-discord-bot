package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.Registry
import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.jda.actor.SlashCommandActor

class ReputationCommand {

    @Command("rep")
    @Description("View a user's reputation")
    fun onReputationCommand(
        actor: SlashCommandActor,
        @Description("User to see reputation") @Optional user: User?
    ) {
        val event = actor.commandEvent()
        val finalUser = user ?: event.user
        val profile = Registry.PROFILES.findByUser(finalUser)
        val reputation = StringBuilder()
        val repMap: Map<Int, Reputation> = profile.reputation.descendingMap()
        val i = intArrayOf(0)
        repMap.forEach { (id: Int?, rep: Reputation) ->
            if (i[0] == 5) return@forEach
            reputation.append("- ")
            if (rep.fromMemberId != null) reputation.append("From <@").append(rep.fromMemberId).append(">, on <t:")
                .append(rep.timestamp).append(":f>") else reputation.append("On <t:").append(rep.timestamp)
                .append(":f>")
            if (rep.fromPostId != null) reputation.append(" in <#").append(rep.fromPostId).append(">")
            reputation.append(" (").append(id).append(")\n")
            i[0]++
        }
        event.replyEmbeds(
            embed()
                .setTitle(finalUser.name + "'s reputation")
                .setDescription("${profile.reputation.size} reputation points")
                .addField(
                    "Last 5 reputation",
                    if (reputation.isEmpty()) "No reputation" else reputation.toString(),
                    false
                )
                .build()
        ).setEphemeral(true).queue()
    }

}