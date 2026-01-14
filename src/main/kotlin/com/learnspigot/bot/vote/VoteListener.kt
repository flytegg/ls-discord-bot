package com.learnspigot.bot.vote

import com.google.common.cache.CacheBuilder
import com.learnspigot.bot.Server
import com.learnspigot.bot.Server.isStaff
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class VoteListener : ListenerAdapter() {

    val cooldown = CacheBuilder.newBuilder()
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build<String, String>()

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        if (event.channel?.idLong == Server.CHANNEL_NEWS.idLong) {
            return event.reply("You cannot use this in the News channel.").setEphemeral(true).queue()
        }

        when (event.name) {
            "Set vote" -> event.run {
                if (event.channel!!.id == Server.CHANNEL_COUNTING.id) // Stop fake counting bullshit
                    return event.reply("You cannot use that in this channel.").setEphemeral(true).queue()

                val member = event.member!!
                if (cooldown.asMap().containsKey(member.id) && !member.isStaff) {
                    return event.reply("You are on cooldown! Please wait.").setEphemeral(true).queue()
                }

                target.apply {
                    addReaction(Server.EMOJI_UPVOTE).queue()
                    addReaction(Server.EMOJI_DOWNVOTE).queue()
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