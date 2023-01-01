package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.editEmbed
import com.learnspigot.bot.manager.ForumManager
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions

// TODO: Award at thread close
class ThreadCloseCommand(private val guild: Guild, private val bot: JDA, private val forumManager: ForumManager) {

    fun threadCloseCommand() {
        guild.upsertCommand("close", "Close a help thread") {
            restrict(guild = true)

            bot.onCommand("close") {
                it.deferReply(true).queue()
                val channel = it.channel!!
                if (channel !is ThreadChannel || channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) {
                    it.editEmbed({
                        title = "Hang on"
                        description = "This isn't a help thread."
                    }).queue()
                    return@onCommand
                }
                forumManager.closeThread(channel)

                if(it.member!!.id == channel.ownerId) {
                    it.editEmbed({
                        description = "Follow the instructions below"
                    }).queue()
                }else {
                    it.hook.editOriginal(MessageEdit("<@${channel.ownerId}>")).queue { _ -> it.hook.deleteOriginal().queue() }
                }
            }
        }.queue()
    }

    fun forceThreadCloseCommand() {
        guild.upsertCommand("forceclose", "Force-close a help thread") {
            restrict(guild = true, DefaultMemberPermissions.DISABLED)

            bot.onCommand("forceclose") {
                val channel = it.channel!!
                if (channel !is ThreadChannel || channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) {
                    it.replyEmbed({
                        title = "Hang on"
                        description = "This isn't a help thread."
                    }, ephemeral = true).queue()
                    return@onCommand
                }
                forumManager.closeThread(channel, true)
                it.replyEmbed({
                    description = "Ticket has been force-closed!"
                }, ephemeral = true).queue()
            }
        }.queue()
    }
}