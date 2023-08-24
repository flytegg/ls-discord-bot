package com.learnspigot.bot.vote

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class VoteListener : ListenerAdapter() {

    override fun onMessageContextInteraction(event: MessageContextInteractionEvent) {
        when (event.name) {
            "Set vote" -> event.run {
                target.apply {
                    addReaction(Server.upvoteEmoji).queue()
                    addReaction(Server.downvoteEmoji).queue()
                }
                reply("Vote reactions were added!").setEphemeral(true).queue()
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