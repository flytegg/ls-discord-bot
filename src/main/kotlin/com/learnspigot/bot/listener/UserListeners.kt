package com.learnspigot.bot.listener

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.entity.SerializedMessage
import com.learnspigot.bot.entity.UserProfile
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse

class UserListeners(guild: Guild, bot: JDA, datastore: Datastore) {

    init {
        bot.listener<MessageReceivedEvent> {
            if(it.channel.id == System.getenv("SUGGESTIONS_CHANNEL_ID")) {
                if (it.author.isBot) return@listener
                val suggestionsChannel = it.guild.getTextChannelById(System.getenv("SUGGESTIONS_CHANNEL_ID"))!!
                it.message.delete().queue()
                suggestionsChannel.sendMessageEmbeds(Embed {
                    title = "Suggestion"
                    description = it.message.contentDisplay
                    color = LearnSpigotBot.EMBED_COLOR

                    field("Submitted by", it.member!!.asMention)
                }).queue {
                    it.addReaction(bot.getEmojiById(System.getenv("EMOJI_YES_ID")) ?: Emoji.fromUnicode("\u2705")).queue()
                    it.addReaction(bot.getEmojiById(System.getenv("EMOJI_NO_ID")) ?: Emoji.fromUnicode("\u274C")).queue()
                }
            }
            if(it.member == null) return@listener
            val profile: UserProfile = datastore.findUserProfile(it.member!!.id)
            profile.messageHistory.add(SerializedMessage.fromDiscordMessage(it.message))
            datastore.save(profile)
        }

        bot.listener<GuildMemberJoinEvent> {
            it.user.openPrivateChannel().queue ({ channel ->
                channel.sendMessageEmbeds(Embed {
                    title = "Welcome to the Discord! :tada:"
                    description = """
                        You have joined the exclusive support community for the "Develop Minecraft Plugins (Java)" Udemy course.
                        
                        :question: Don't have the course? Grab it @ https://learnspigot.com
                        
                        :star: Have it? Follow the instructions in the ${guild.getTextChannelById(System.getenv("VERIFY_CHANNEL_ID").toLong())!!.asMention} channel!
                        
                        Without verifying, you can still read the server but won't have access to our 24/7 support team and dozens of tutorials and projects.
                        
                        PS: Use our pastebin. Pastes do not expire! https://paste.learnspigot.com
                    """.trimIndent()
                    color = LearnSpigotBot.EMBED_COLOR
                }).queue()
            }, ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
        }
    }
}
