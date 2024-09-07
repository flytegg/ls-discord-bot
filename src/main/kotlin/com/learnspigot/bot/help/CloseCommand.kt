package com.learnspigot.bot.help

import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.ThreadMember
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class CloseCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    companion object {
        val knowledgebasePostsUsed = mutableMapOf<String, MutableSet<String>>()
    }

    @Command(
        name = "close", description = "Close a help post"
    )
    fun onCloseCommand(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return
        if (event.channelType != ChannelType.GUILD_PUBLIC_THREAD) return

        val channel = event.guildChannel.asThreadChannel()
        if (channel.parentChannel.id != Server.helpChannel.id) return

        if (event.member!!.id != channel.ownerId && !event.member!!.roles.contains(Server.managementRole)) {
            event.reply("You cannot close this thread!").setEphemeral(true).queue()
            return
        }

        event.deferReply().queue()

        val contributors: List<Member> =
            channel.retrieveThreadMembers().complete().asSequence()
                // excludes the author of the channel
                .filter { member: ThreadMember -> member.id != channel.ownerId }
                // excludes bots
                .filter { member: ThreadMember -> !member.user.isBot }
                // excludes users that haven't sent a single message to this channel (i.e: users that clicked the 'follow post' button)
                .filter { member: ThreadMember ->
                    val messageHistory = channel.iterableHistory.complete()
                    messageHistory.any { it.author.id == member.id }}
                .take(25).map { it.member }.toList()

        if (contributors.isEmpty()) {
            event.hook.sendMessageEmbeds(
                embed().setTitle(event.member!!.effectiveName + " has closed the thread")
                    .setDescription("Listing no contributors.").build()
            ).complete()
            channel.manager.setArchived(true).setLocked(true).complete()
            return
        }

        val owner = channel.owner
        if (owner != null) channel.sendMessage(owner.asMention).queue { it.delete().queue() }

        event.hook.sendMessageEmbeds(
            embed().setTitle("Who helped you solve your issue?").setDescription(
                """
                                Please select people from the dropdown who helped solve your issue.
                                                                
                                Once you've selected contributors, click the Close button to close your post.
                    """
            ).build()
        ).addActionRow(
            StringSelectMenu.create(channel.id + "-contributor-selector")
                .setPlaceholder("Select the people that helped solve your issue").setRequiredRange(0, 25)
                .addOptions(contributors.map { member: Member ->
                    SelectOption.of(
                        member.effectiveName, member.id
                    ).withDescription(member.user.name)
                })
                .addOptions(
                    knowledgebasePostsUsed[channel.id]?.map { postId ->
                        SelectOption.of(
                            Server.guild.getThreadChannelById(postId)?.name ?: "", "knowledgebase:$postId"
                        ).withDescription("Knowledgebase Post")
                    } ?: listOf()
                ).build()
        ).addActionRow(Button.danger(channel.id + "-close-button", "Close"))
            .queue { message: Message -> profileRegistry.messagesToRemove[event.channel.id] = message }
    }
}