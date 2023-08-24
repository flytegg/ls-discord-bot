package com.learnspigot.bot.vote

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*
import java.util.concurrent.TimeUnit

class VoteListener : ListenerAdapter() {

    val cooldown = CacheBuilder.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build<String, String>()

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        if (event.channel!!.id == Environment.get("NEWS_CHANNEL_ID")) {
            event.reply("You cannot use this in the News channel.").setEphemeral(true).queue()
            return
        }

        println(event.member!!.effectiveName + " added vote")

        when (event.name) {
            "Set vote" -> event.run {
                val member = event.member!!
                val roles = member.roles
                if (cooldown.asMap().containsKey(member.id) &&
                    !roles.contains(event.jda.getRoleById(Environment.get("MANAGEMENT_ROLE_ID"))) &&
                        !roles.contains(event.jda.getRoleById(Environment.get("STAFF_ROLE_ID")))) {
                    event.reply("You are on cooldown! Please wait.").setEphemeral(true).queue()
                    return
                }

                target.apply {
                    addReaction(Server.upvoteEmoji).queue()
                    addReaction(Server.downvoteEmoji).queue()
                }
                reply("Vote reactions were added!").setEphemeral(true).queue()

                cooldown.put(member.id, "Dummy")
            }
            "Set Tutorial vote" -> event.run {
                target.apply {
                    addReaction(Emoji.fromUnicode("1\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("2\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("3\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("4\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("5\uFE0F⃣")).queue()
                }
                reply("Vote reactions were added!").setEphemeral(true).queue()
            }
            "Set Project vote" -> event.run {
                target.apply {
                    addReaction(Emoji.fromUnicode("1\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("2\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("3\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("4\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("5\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("6\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("7\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("8\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("9\uFE0F⃣")).queue()
                    addReaction(Emoji.fromUnicode("\uD83D\uDD1F")).queue()
                }
                reply("Vote reactions were added!").setEphemeral(true).queue()
            }
        }
    }

}