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
                if (guild.getMemberById(it.user.id)!!.roles.contains(guild.getRoleById("879039133568356363")) && bot.getThreadChannelById(
                        it.channel!!.id
                    )!!.parentChannel.id == "1054894764291534930"
                ) {
                    val tutorialEmojis = mutableListOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣")
                    val projectEmojis =
                        mutableListOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟")
                    // support team role id:804147192758534164
                    // for review channel id : 1062442195124224000
                    val type = bot.getThreadChannelById(it.channel!!.id)!!.appliedTags[0].name
                    var message: Message? = null
                    // vote channel id: "1069383432448258098"
                    bot.getTextChannelById("858353032251965460")!!.sendMessageEmbeds(
                        Embed {
                            title = "**VOTE FOR REPUTATION**"
                            description =
                                "<@${it.user.id}> has just created a ${if (type == "Tutorial") "tutorial" else "project"}, please vote what reputation to give them between ${if (type == "Tutorial") "0-5" else "0-10"} based on quality and your personal factors. \n\n [Link](${
                                    bot.getThreadChannelById(
                                        it.channel!!.id
                                    )!!.jumpUrl
                                })\n\nThe vote will end in <t:${(System.currentTimeMillis() / 1000) + 86400}:R>"
                            color = EMBED_COLOR
                        }
                    ).queue { m ->
                        message = m
                        if (type == "Tutorial")
                            for (x in tutorialEmojis)
                                m.addReaction(Emoji.fromUnicode(x)).queue()
                        else
                            for (x in projectEmojis)
                                m.addReaction(Emoji.fromUnicode(x)).queue()
                    }
                    Timer().schedule(10000) {
                        var sum = 0
                        val usersReacted = mutableListOf<String>()
                        val usersReaction = mutableListOf<Int>()
                        // get count of reaction
                        if (type == "Tutorial") {
                            for (emo in tutorialEmojis.indices.reversed()) {
                                for (user in bot.getTextChannelById("858353032251965460")!!
                                    .retrieveMessageById(message!!.id)
                                    .complete().retrieveReactionUsers(Emoji.fromUnicode(tutorialEmojis[emo])))
                                    if (!usersReacted.contains(user.id) && user.id != bot.selfUser.id) {
                                        usersReacted.add(user.id)
                                        usersReaction.add(emo)
                                    }
                            }
                            usersReaction.forEach { num -> sum += num }
                        } else {
                            for (emo in projectEmojis.indices.reversed()) {
                                for (user in bot.getTextChannelById("858353032251965460")!!
                                    .retrieveMessageById(message!!.id)
                                    .complete().retrieveReactionUsers(Emoji.fromUnicode(projectEmojis[emo])))
                                    if (!usersReacted.contains(user.id) && user.id != bot.selfUser.id) {
                                        usersReacted.add(user.id)
                                        usersReaction.add(emo)
                                    }
                            }
                            usersReaction.forEach { num -> sum += num }
                        }
                        val reputation = sum / usersReacted.size
                        val profile: UserProfile = datastore.findUserProfile(it.user.id)
                        profile.addRep(
                            leaderboardManager,
                            it.guild!!,
                            type,
                            reputation
                        )
                        datastore.save(profile)
                        it.messageChannel.sendMessage("The vote has ended, <@${it.user.id}> received $reputation reputation${if (reputation > 1) "s" else ""}")
                            .queue()
                        val tags = mutableListOf<ForumTag>()
                        bot.getThreadChannelById(it.channel!!.id)!!.appliedTags.forEach { t -> tags.add(t) }
                        tags.add(bot.getForumChannelById(1054894764291534930)!!.availableTags.find { t -> t.name == "Approved" }!!)
                        bot.getThreadChannelById(it.channel!!.id)!!.manager.setAppliedTags(tags).queue()
                        message!!.editMessageEmbeds(
                            Embed {
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
                it.replyEmbed({
                    title = "Success"
                    description = "A vote has been started, results will be announced in 24 hours"
                    color = EMBED_COLOR
                }).queue()
            }
        }.queue()
    }
}