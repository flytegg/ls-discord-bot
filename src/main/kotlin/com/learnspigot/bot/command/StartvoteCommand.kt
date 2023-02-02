package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.manager.LeaderboardManager
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.forums.ForumTag
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.util.*
import kotlin.concurrent.schedule

class StartvoteCommand(
    private val guild: Guild,
    private val bot: JDA,
    private val datastore: Datastore,
    private val leaderboardManager: LeaderboardManager
) {
    fun startvoteCommand() {
        guild.upsertCommand("startvote", "Start a vote for project or tutorial") {
            restrict(guild = true)
            bot.onCommand("startvote") {
                if (guild.getMemberById(it.user.id)!!.roles.contains(guild.getRoleById(System.getenv("SUPPORT_ROLE_ID"))) &&
                    bot.getThreadChannelById(
                        it.channel!!.id
                    )?.parentChannel?.id != null && bot.getThreadChannelById(
                        it.channel!!.id
                    )!!.parentChannel.id == System.getenv("FOR_REVIEW_CHANNEL_ID")
                ) {
                    it.replyEmbed({
                        title = "Success"
                        description = "A vote has been started, results will be announced in 24 hours"
                        color = EMBED_COLOR
                    }).queue()
                    val tutorialEmojis = mutableListOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣")
                    val projectEmojis =
                        mutableListOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟")
                    val type = bot.getThreadChannelById(it.channel!!.id)!!.appliedTags[0].name
                    var message: Message? = null
                    bot.getTextChannelById(System.getenv("VOTE_CHANNEL_ID"))!!.sendMessageEmbeds(Embed {
                        title = "**VOTE FOR REPUTATION**"
                        description =
                            "<@${it.user.id}> has just created a ${if (type == "Tutorial") "tutorial" else "project"}, please vote what reputation to give them between ${if (type == "Tutorial") "0-5" else "0-10"} based on quality and your personal factors. \n\n [Link](${
                                bot.getThreadChannelById(
                                    it.channel!!.id
                                )!!.jumpUrl
                            })\n\nThe vote will end in <t:${(System.currentTimeMillis() / 1000) + 86400}:R>"
                        color = EMBED_COLOR
                    }).queue { m ->
                        message = m
                        if (type == "Tutorial") tutorialEmojis.forEach { e ->
                            m.addReaction(Emoji.fromUnicode(e)).queue()
                        }
                        else projectEmojis.forEach { e -> m.addReaction(Emoji.fromUnicode(e)).queue() }
                    }
                    Timer().schedule(10000) {
                        var sum = 0
                        val usersReacted = mutableListOf<String>()
                        val usersReaction = mutableListOf<Int>()
                        if (type == "Tutorial") {
                            for (emo in tutorialEmojis.indices.reversed()) {
                                for (user in bot.getTextChannelById(System.getenv("VOTE_CHANNEL_ID"))!!
                                    .retrieveMessageById(message!!.id).complete()
                                    .retrieveReactionUsers(Emoji.fromUnicode(tutorialEmojis[emo]))) if (!usersReacted.contains(
                                        user.id
                                    ) && user.id != bot.selfUser.id
                                ) {
                                    usersReacted.add(user.id)
                                    usersReaction.add(emo)
                                }
                            }
                        } else {
                            for (emo in projectEmojis.indices.reversed()) {
                                for (user in bot.getTextChannelById(System.getenv("VOTE_CHANNEL_ID"))!!
                                    .retrieveMessageById(message!!.id).complete()
                                    .retrieveReactionUsers(Emoji.fromUnicode(projectEmojis[emo]))) if (!usersReacted.contains(
                                        user.id
                                    ) && user.id != bot.selfUser.id
                                ) {
                                    usersReacted.add(user.id)
                                    usersReaction.add(emo)
                                }
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
                        val tags = mutableListOf<ForumTag>()
                        bot.getThreadChannelById(it.channel!!.id)!!.appliedTags.forEach { t -> tags.add(t) }
                        tags.add(bot.getForumChannelById(System.getenv("FOR_REVIEW_CHANNEL_ID"))!!.availableTags.find { t -> t.name == "Approved" }!!)
                        bot.getThreadChannelById(it.channel!!.id)!!.manager.setAppliedTags(tags).queue()
                        message!!.editMessageEmbeds(Embed {
                            title = "**VOTE FOR REPUTATION**"
                            description =
                                "<@${it.user.id}> has just created a ${if (type == "Tutorial") "tutorial" else "project"}, please vote what reputation to give them between ${if (type == "Tutorial") "0-5" else "0-10"} based on quality and your personal factors. \n\n [Link](${
                                    bot.getThreadChannelById(
                                        it.channel!!.id
                                    )!!.jumpUrl
                                })\n\nThe vote has **ended**"
                            color = EMBED_COLOR
                        }).queue()
                    }
                } else {
                    it.reply("Please be a support team member and use it in the correct channel").queue()
                }

            }
        }.queue()
    }
}