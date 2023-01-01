package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.emoji.Emoji

class SuggestionsCommand(guild: Guild, private val bot: JDA) {

    init {
        guild.upsertCommand("suggest", "Make a suggestion to the course") {
            restrict(guild = true)
            option<String>("suggestion", "Your suggestion", required = true)

            bot.onCommand("suggest") {
                it.deferReply().queue()
                val suggestion = it.getOption("suggestion")!!.asString
                val suggestionsChannel = it.guild!!.getTextChannelById(System.getenv("SUGGESTIONS_CHANNEL_ID"))!!

                suggestionsChannel.sendMessageEmbeds(Embed {
                    title = "Suggestion"
                    description = suggestion
                    color = EMBED_COLOR

                    field("Submitted by", it.member!!.asMention)
                }).queue {
                    it.addReaction(bot.getEmojiById(System.getenv("EMOJI_YES_ID")) ?: Emoji.fromUnicode("\u2705")).queue()
                    it.addReaction(bot.getEmojiById(System.getenv("EMOJI_NO_ID")) ?: Emoji.fromUnicode("\u274C")).queue()
                }
                it.replyEmbed({
                    title = "Success"
                    description = "Your suggestion is now in ${suggestionsChannel.asMention}"
                    color = EMBED_COLOR
                }, ephemeral = true).queue()
            }
        }.queue()
    }
}