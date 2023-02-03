package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.manager.KnowledgeBaseManager
import com.learnspigot.bot.util.KnowledgeBaseType
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

class StartVoteCommand(
    private val guild: Guild,
    private val bot: JDA,
    private val knowledgeBaseManager: KnowledgeBaseManager
) {
    fun startVoteCommand() {
        guild.upsertCommand("startvote", "Start a vote for project or tutorial") {
            restrict(guild = true, Permission.ADMINISTRATOR)
            bot.onCommand("startvote") {
                if(!it.member!!.roles.contains(guild.getRoleById(System.getenv("SUPPORT_ROLE_ID")))) return@onCommand
                val channel = it.channel as? ThreadChannel ?: return@onCommand
                if(channel.parentChannel.id != System.getenv("FOR_REVIEW_CHANNEL_ID")) return@onCommand

                val typeName = (it.channel as ThreadChannel).appliedTags
                    .find { tag -> KnowledgeBaseType.values()
                        .map { it.displayName }.contains(tag.name) }?.name
                if(typeName == null) {
                    it.replyEmbed({
                        title = "Error"
                        description = "Unable to find type tag. Please make sure it has a valid tag"
                    })
                    return@onCommand
                }

                val type: KnowledgeBaseType = when(typeName) {
                    "Tutorial" -> KnowledgeBaseType.TUTORIAL
                    "Project" -> KnowledgeBaseType.PROJECT
                    else -> throw IllegalStateException("Invalid tag")
                }

                knowledgeBaseManager.startVote(it.channel as ThreadChannel, it.member!!, type)

                it.replyEmbed({
                    title = "Success"
                    description = "A vote has been started, results will be announced in 24 hours"
                }).queue()
            }
        }.queue()
    }


}
