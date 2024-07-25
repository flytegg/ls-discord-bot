package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.profile
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ReputationCommand {

    @Command(name = "rep", description = "View a user's reputation")
    fun onReputationCommand(
        event: SlashCommandInteractionEvent,
        @Description("User to see reputation") @Optional user: User?
    ) {
        val finalUser = user ?: event.user
        val profile = finalUser.profile
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