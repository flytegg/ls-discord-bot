package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.util.KnowledgeBaseType
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.util.*
import kotlin.concurrent.schedule
import kotlin.time.Duration.Companion.days

class KnowledgeBaseManager(private val bot: JDA, private val datastore: Datastore, private val leaderboardManager: LeaderboardManager) {

    private val emojis = listOf("0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟")

    fun startVote(channel: ThreadChannel, user: Member, type: KnowledgeBaseType) {
        val message: Message = bot.getTextChannelById(System.getenv("VOTE_CHANNEL_ID"))!!
            .sendMessageEmbeds(Embed {
                title = "Vote for reputation"
                description =
                    "<@${user.id}> has just created a ${type.displayName.lowercase()}, please vote what reputation to give them between ${type.range.first} - ${type.range.last}" +
                            "based on quality and other aspects you find important. \n\n [Link](${channel.jumpUrl})" +
                            "\n\nThe vote will end in <t:${(System.currentTimeMillis() / 1000) + 86400}:R>"
                color = EMBED_COLOR
            }).complete()

        for (i in type.range) {
            message.addReaction(
                Emoji.fromUnicode(emojis[i])
            ).queue()
        }

        Timer().schedule(1.days.inWholeMilliseconds) {
            var totalUsers = 0
            var totalReacted = 0
            for (i in type.range) {
                val reactors = message.retrieveReactionUsers(Emoji.fromUnicode(emojis[i])).complete().filter { it.id != bot.selfUser.id }
                repeat(reactors.size) {
                    totalUsers++
                    totalReacted += i
                }
            }

            val reputation = totalReacted / totalUsers
            val profile: UserProfile = datastore.findUserProfile(user.id)
            profile.addKnowledgeBaseRep(
                leaderboardManager, channel.guild, type, reputation
            )
            datastore.save(profile)
            channel.sendMessage("The vote has ended, <@${user.id}> received $reputation reputation${if (reputation > 1) "s" else ""}")
                .queue()

            val tag = bot.getForumChannelById(System.getenv("FOR_REVIEW_CHANNEL_ID"))!!.getAvailableTagsByName("Approved", false).first()
            channel.manager.setAppliedTags(channel.appliedTags.also { it.add(tag) }).queue()
            message.editMessageEmbeds(Embed {
                title = "Received Reputation"
                description = "<@${channel.ownerId}> received $reputation reputation for this ${type.displayName}"
                color = EMBED_COLOR
            }).queue()
        }
    }
}