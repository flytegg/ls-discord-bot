package com.learnspigot.bot.knowledgebase

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.Permission
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission

class EndPollCommand {

    @Command("endpoll")
    @Description("Ends the current poll")
    fun onEndPollCommand(actor: SlashCommandActor, @Named("message-id") @Description("Poll to end!") messageId: String) {
        val event = actor.commandEvent()
        event.channel.retrieveMessageById(messageId).queue { message ->
            if (message.poll == null) {
                event.reply("There is no poll running in this channel!").setEphemeral(true).queue()
                return@queue
            }

            message.endPoll().queue()
            event.reply("Poll ended!").setEphemeral(true).queue()
        }
    }

}