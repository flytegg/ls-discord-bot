package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.manager.ForumManager
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

// TODO: Award at thread close
class ThreadCloseCommand(guild: Guild, bot: JDA, forumManager: ForumManager) {

    init {
        guild.upsertCommand("close", "Close a help thread") {
            restrict(guild = true)

            bot.onCommand("close") {
                val channel = it.channel!!
                if (channel !is ThreadChannel || channel.parentChannel.id != System.getenv("HELP_CHANNEL_ID")) {
                    it.replyEmbed({
                        title = "Hang on"
                        description = "This isn't a help thread."
                    }, ephemeral = true).queue()
                    return@onCommand
                }
                forumManager.closeThread(channel)

                if(it.member!!.id == channel.ownerId) {
                    it.replyEmbed({
                        description = "Follow the instructions below"
                    }, ephemeral = true).queue()
                }else {
                    it.reply_("<@${channel.ownerId}>").queue { it.deleteOriginal().queue() }
                }

            }
        }.queue()
    }
}