package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ReputationCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Command(name = "rep", description = "View a user's reputation")
    fun onReputationCommand(event: SlashCommandInteractionEvent, @Optional user: User = event.user) {
        val profile = profileRegistry.findByUser(user)
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
                .setTitle(user.name + "'s reputation")
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