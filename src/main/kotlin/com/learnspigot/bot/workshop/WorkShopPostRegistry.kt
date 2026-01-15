package com.learnspigot.bot.workshop

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isManager
import com.learnspigot.bot.util.closeAndLock
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class WorkShopPostRegistry {

    /** Holds a list of channels that are ready to be closed so that our channel keep-alive listener does not stop them from being closed */
    val channelsMarkedForClosing: MutableSet<Long> = mutableSetOf()

    val posts: HashMap<String, String> = hashMapOf() // post-id / owner-id

    init {
//        CompletableFuture.runAsync({
            for (channel in Server.CHANNEL_WORKSHOP.threadChannels) {
                if (channel.owner == null) {
                    Registry.WORKSHOP.channelsMarkedForClosing.add(channel.idLong)
                    channel.sendMessageEmbeds(embed().setTitle("Workshop close.").setDescription("Closing workshop because owner isn't in the server.").build()).queue {
                        channel.closeAndLock()
                    }
                    continue
                }
                posts[channel.id] = channel.owner!!.id
            }

//        }, Executors.newCachedThreadPool())
    }

    fun closeCommand(event: SlashCommandInteractionEvent) {
        val channel = event.guildChannel.asThreadChannel()

        if (event.member!!.id != channel.ownerId && !event.member.isManager)
            return event.replyEmbeds(embed().setTitle("You cannot close this workshop").build()).setEphemeral(true).queue()

        event.deferReply().queue()

        event.hook.sendMessageEmbeds(embed().setTitle("Close confirmation")
            .setDescription("Are you sure you want to close this workshop?").build()
        ).addComponents(ActionRow.of(Button.danger(channel.id + "-close-button", "Close")))
            .queue { message: Message -> Registry.PROFILES.messagesToRemove[event.channel.id] = message }
    }
}