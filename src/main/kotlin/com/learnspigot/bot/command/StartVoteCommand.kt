package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.manager.LeaderboardManager
import com.learnspigot.bot.util.KnowledgeBaseTypes
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class StartVoteCommand(
    private val guild: Guild,
    private val bot: JDA,
    private val datastore: Datastore,
    private val leaderboardManager: LeaderboardManager
) {
    fun startVoteCommand() {
        guild.upsertCommand("startvote", "Start a vote for project or tutorial") {
            restrict(guild = true)
            bot.onCommand("startvote") {
                if (guild.getMemberById(it.user.id)!!.roles.contains(guild.getRoleById(System.getenv("SUPPORT_ROLE_ID"))) && bot.getThreadChannelById(
                        it.channel!!.id
                    )?.parentChannel?.id != null && bot.getThreadChannelById(it.channel!!.id)!!.parentChannel.id == System.getenv(
                        "FOR_REVIEW_CHANNEL_ID"
                    )
                ) {
                    it.replyEmbed({
                        title = "Success"
                        description = "A vote has been started, results will be announced in 24 hours"
                        color = EMBED_COLOR
                    }).queue()
                    val tutorialEmojis = mutableListOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣")
                    val projectEmojis =
                        mutableListOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟")
                    val type = KnowledgeBaseTypes.valueOf(bot.getThreadChannelById(it.channel!!.id)!!.appliedTags.find { tag -> tag.name == "Tutorial" || tag.name == "Project" }?.name!!.uppercase())

                    var message: Message? = null
                    bot.getTextChannelById(System.getenv("VOTE_CHANNEL_ID"))!!
                        .sendMessageEmbeds(voteEmbed(type, it, false)).queue { msg ->
                            message = msg
                            (if (type == KnowledgeBaseTypes.TUTORIAL) tutorialEmojis else projectEmojis).forEach { emoji ->
                                msg.addReaction(
                                    Emoji.fromUnicode(emoji)
                                ).queue()
                            }
                        }
                    Timer().schedule(TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
                        var sum = 0
                        val usersReacted = mutableListOf<String>()
                        val usersReaction = mutableListOf<Int>()
                        for (emoji in (if (type == KnowledgeBaseTypes.TUTORIAL) tutorialEmojis else projectEmojis).indices.reversed()) {
                            for (user in bot.getTextChannelById(System.getenv("VOTE_CHANNEL_ID"))!!
                                .retrieveMessageById(message!!.id).complete()
                                .retrieveReactionUsers(Emoji.fromUnicode((if (type == KnowledgeBaseTypes.TUTORIAL) tutorialEmojis else projectEmojis)[emoji]))) if (!usersReacted.contains(user.id) && user.id != bot.selfUser.id
                            ) {
                                usersReacted.add(user.id)
                                usersReaction.add(emoji)
                            }

                        }
                        usersReaction.forEach { num -> sum += num }
                        val reputation = sum / usersReacted.size
                        val profile: UserProfile = datastore.findUserProfile(it.user.id)
                        profile.addRep(
                            leaderboardManager, it.guild!!, type, reputation
                        )
                        datastore.save(profile)
                        it.messageChannel.sendMessage("The vote has ended, <@${it.user.id}> received $reputation reputation${if (reputation > 1) "s" else ""}")
                            .queue()
                        val tagsCurrently = mutableListOf<ForumTag>()
                        bot.getThreadChannelById(it.channel!!.id)!!.appliedTags.forEach { t -> tagsCurrently.add(t) }
                        tagsCurrently.add(bot.getForumChannelById(System.getenv("FOR_REVIEW_CHANNEL_ID"))!!.availableTags.find { t -> t.name == "Approved" }!!)
                        bot.getThreadChannelById(it.channel!!.id)!!.manager.setAppliedTags(tagsCurrently).queue()
                        message!!.editMessageEmbeds(voteEmbed(type, it, true)).queue()
                    }
                } else
                    it.reply("Please be a support team member and use it in the correct channel").queue()


            }
        }.queue()
    }

    private fun voteEmbed(type: KnowledgeBaseTypes, interaction: GenericCommandInteractionEvent, ended: Boolean): MessageEmbed {
        return Embed {
            title = "**VOTE FOR REPUTATION**"
            description =
                "<@${interaction.user.id}> has just created a ${type.lowercaseName}, please vote what reputation to give them between ${if (type == KnowledgeBaseTypes.TUTORIAL) "0-5" else "0-10"} based on quality and other aspects you find important. \n\n [Link](${
                    bot.getThreadChannelById(
                        interaction.channel!!.id
                    )!!.jumpUrl
                })\n\n${if (ended) "The vote has **ended**" else "The vote will end in <t:${(System.currentTimeMillis() / 1000) + 86400}:R>"}"
            color = EMBED_COLOR
        }
    }
}
