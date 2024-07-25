package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.database.profile.addReputation
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.isManager
import com.learnspigot.bot.util.owns
import com.learnspigot.bot.util.replyEphemeral
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CloseListener : ListenerAdapter() {

    companion object {
        val contributorSelectorCache: MutableMap<String, List<String>> = HashMap()
    }

    private fun Member.canClose(thread: ThreadChannel) = owns(thread) || isManager

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.componentId != event.channel.id + "-contributor-selector") return
        val thread = event.channel.asThreadChannel()
        val member = event.member ?: return

        if (!member.canClose(thread)) return event.replyEphemeral("You cannot close this thread!")

        event.interaction.deferEdit().queue()
        contributorSelectorCache[event.channel.id] = event.values
    }

    private fun EmojiUnion.asDigit(): Int? = asUnicode().name[0].takeIf(Character::isDigit)?.digitToInt()?.minus('0'.digitToInt())

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!event.componentId.endsWith("-close-button")) return
        val thread = event.channel.asThreadChannel()
        val member = event.member ?: return

        if (!member.canClose(thread)) return event.replyEphemeral("You cannot close this thread!")

        event.editButton(event.button.asDisabled()).complete()
        val contributors = contributorSelectorCache[event.channel.id] ?: mutableListOf()

        val reputation = thread.getHistoryFromBeginning(1).complete().retrievedHistory[0].reactions
            .find { it.isSelf && it.emoji.asDigit() != null }
            ?.emoji?.asDigit() ?: 1

        contributors.forEach { contributorId ->
            val contributor = if (contributorId.startsWith("knowledgebase:"))
                Server.guild.getThreadChannelById(contributorId.removePrefix("knowledgebase:"))?.owner ?: return@forEach
            else
                Server.guild.retrieveMemberById(contributorId).complete()

            contributor.addReputation(thread.ownerId, thread.id, reputation)
        }

        CloseCommand.messagesToRemove[thread.id]?.delete()?.queue()
        CloseCommand.messagesToRemove.remove(thread.id)
        CloseCommand.knowledgebasePostsUsed.remove(thread.id)

        event.channel.asThreadChannel().getHistoryFromBeginning(2).complete().retrievedHistory[0].delete().complete()

        event.channel.sendMessageEmbeds(embed()
            .setTitle(event.member!!.effectiveName + " has closed the thread")
            .setDescription("Listing ${if (contributors.isEmpty()) "no contributors." else contributors.joinToString(", ") {
                if (it.startsWith("knowledgebase:")) "<#${it.removePrefix("knowledgebase:")}>" else "<@$it>"
            } + " as contributors."}")
            .build()).complete()

        thread.manager.setArchived(true).setLocked(true).complete()
    }

}