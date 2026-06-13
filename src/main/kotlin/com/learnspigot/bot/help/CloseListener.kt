package com.learnspigot.bot.help

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.Server.isStudent
import com.learnspigot.bot.util.*
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateArchivedEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class CloseListener : ListenerAdapter() {

    private inline val profileRegistry get() = Registry.PROFILES

    private fun Member.hasClosePermission(channel: ThreadChannel): Boolean = owns(channel) || isManager

    companion object {
        private val postsBeingClosed = mutableSetOf<Long>()
        fun closingPost(id: Long) = postsBeingClosed.add(id)
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.componentId != event.channel.id + "-contributor-selector") return
        val channel = event.channel.asThreadChannel()
        val member = event.member ?: return

        if (!member.hasClosePermission(channel)) {
            event.reply("You cannot close this thread!").setEphemeral(true).queue()
            return
        }

        event.interaction.deferEdit().queue()

        profileRegistry.contributorSelectorCache[event.channel.id] = event.values
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        // Handle support "confirm close" button press.
        if (event.componentId == "confirm-no-contributors") {
            event.deferEdit().queue()
            return event.message.delete().queue()
        }

        if (event.channel.type != ChannelType.GUILD_PUBLIC_THREAD) return
        val channel = event.channel.asThreadChannel()
        if (channel.parentChannel.id != Server.CHANNEL_HELP.id && channel.parentChannel.id != Server.CHANNEL_CODE_REVIEW.id) return
        if (!event.componentId.endsWith("-close-button") && !event.componentId.startsWith(channel.id)) return
        val clicker = event.member ?: return

        if (!clicker.hasClosePermission(channel)) {
            return event.replyEphemeral("You cannot close this thread!")
        }

        event.editButton(event.button.asDisabled()).complete()

        postsBeingClosed.add(channel.idLong)

        val contributors = profileRegistry.contributorSelectorCache[event.channel.id] ?: mutableListOf()

        if (contributors.isEmpty()) {
            // We know that by this stage, there were *potential* contributors because when running /close with no *potential* contributors, there is no button to press.
            Server.CHANNEL_ALERTS.sendMessageEmbeds(
                embed().setTitle("Thread Closed With No Contributors").setDescription("${channel.asMention} has been closed listing no available contributors.").setFooter("Please confirm this is intentional.").build()
            ).addComponents(
                ActionRow.of(Button.success("confirm-no-contributors", "Confirm"))
            ).queueAfter(5, TimeUnit.SECONDS)
        }

        var reputation = 1
        channel.getHistoryFromBeginning(1).complete().retrievedHistory[0].reactions.forEach {
            if (it.isSelf && it.emoji.asUnicode().name.toCharArray()[0].isDigit()) {
                reputation = it.emoji.asUnicode().name.toCharArray()[0].toInt() - '0'.toInt()
            }
            return@forEach
        }

        val isCreatorStudent = channel.owner.isStudent
        reputation *= if (isCreatorStudent) 2 else 1

        contributors.forEach { contributor ->
            if (contributor.startsWith("knowledgebase:")) {
                val post = Server.GUILD.getThreadChannelById(contributor.removePrefix("knowledgebase:"))
                post?.owner?.user?.let { user ->
                    profileRegistry.findByUser(user).addReputation(user, channel.ownerId, channel.id, reputation)
                }
            } else {
                val user = event.guild!!.retrieveMemberById(contributor).complete().user
                profileRegistry.findByUser(user).addReputation(user, channel.ownerId, channel.id, reputation)
            }
        }

        profileRegistry.messagesToRemove[channel.id]?.delete()?.queue()
        CloseCommand.knowledgebasePostsUsed.remove(channel.id)

        event.channel.asThreadChannel().getHistoryFromBeginning(2).complete().retrievedHistory[0].delete().complete()

        event.channel.sendMessageEmbeds(embed()
            .setTitle(event.member!!.effectiveName + " has closed the thread")
            .setDescription("Listing ${if (contributors.isEmpty()) "no contributors." else contributors.joinToString(", ") {
                if (it.startsWith("knowledgebase:")) "<#${it.removePrefix("knowledgebase:")}>" else "<@$it>"
            } + " as contributors."}")
            .build()).complete()

        channel.closeAndLock()
    }

    // Listen for help posts being closed. Verify they are closed intentionally.
    override fun onChannelUpdateArchived(e: ChannelUpdateArchivedEvent) {
        if (e.channelType != ChannelType.GUILD_PUBLIC_THREAD) return
        if (e.newValue != true) return
        val channel = e.channel.asThreadChannel().takeIf { Server.CHANNEL_HELP.isChannel(it.parentChannel) } ?: return

        if (!postsBeingClosed.remove(channel.idLong)) {
            // Post has not been closed with /close
            Server.CHANNEL_ALERTS.sendMessageEmbeds(
                embed().setTitle("Thread Closed Manually").setDescription("${channel.asMention} has been closed manually").setFooter("Please confirm this is intentional.").build()
            ).addComponents(
                ActionRow.of(Button.success("confirm-no-contributors", "Confirm"))
            ).queue()
        }
    }

}