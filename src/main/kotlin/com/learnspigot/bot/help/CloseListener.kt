package com.learnspigot.bot.help

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class CloseListener : ListenerAdapter() {

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.componentId != event.channel.id + "-contributor-selector") return
        val channel = event.channel.asThreadChannel()

        if (event.member!!.id != channel.ownerId && !event.member!!.roles.contains(Server.managementRole)) {
            event.reply("You cannot close this thread!").setEphemeral(true).queue()
            return
        }

        event.interaction.deferEdit().queue()

        profileRegistry.contributorSelectorCache[event.channel.id] = event.values
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!event.componentId.endsWith("-close-button")) return
        val channel = event.channel.asThreadChannel()

        if (event.member!!.id != channel.ownerId && !event.member!!.roles.contains(Server.managementRole)) {
            event.reply("You cannot close this thread!").setEphemeral(true).queue()
            return
        }

        event.editButton(event.button.asDisabled()).complete()

        val contributors = profileRegistry.contributorSelectorCache[event.channel.id] ?: mutableListOf()

        var reputation = 1
        channel.getHistoryFromBeginning(1).complete().retrievedHistory[0].reactions.forEach {
            if (it.isSelf && it.emoji.asUnicode().name.toCharArray()[0].isDigit()) {
                reputation = it.emoji.asUnicode().name.toCharArray()[0].toInt() - '0'.toInt()
            }
            return@forEach
        }

        contributors.forEach { contributor ->
            if (contributor.startsWith("knowledgebase:")) {
                val post = Server.guild.getThreadChannelById(contributor.removePrefix("knowledgebase:"))
                post?.owner?.user?.let { user ->
                    profileRegistry.findByUser(user).addReputation(user, channel.ownerId, channel.id, reputation)
                }
            } else {
                val user = event.guild!!.retrieveMemberById(contributor).complete().user
                profileRegistry.findByUser(user).addReputation(user, channel.ownerId, channel.id, reputation)
            }
        }

        CloseCommand.messagesToRemove[channel.id]?.delete()?.queue()
        CloseCommand.messagesToRemove.remove(channel.id)
        CloseCommand.knowledgebasePostsUsed.remove(channel.id)

        event.channel.asThreadChannel().getHistoryFromBeginning(2).complete().retrievedHistory[0].delete().complete()

        event.channel.sendMessageEmbeds(embed()
            .setTitle(event.member!!.effectiveName + " has closed the thread")
            .setDescription("Listing ${if (contributors.isEmpty()) "no contributors." else contributors.joinToString(", ") {
                if (it.startsWith("knowledgebase:")) "<#${it.removePrefix("knowledgebase:")}>" else "<@$it>"
            } + " as contributors."}")
            .build()).complete()

        channel.manager.setArchived(true).setLocked(true).complete()
    }

}