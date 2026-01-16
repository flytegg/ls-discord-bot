package com.learnspigot.bot.counting

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.getEmojiReactionCount
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class VoteBanListener : ListenerAdapter() {

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.channel.id != Server.CHANNEL_COUNTING.id) return
        if (event.user == null) return
        if (event.user!!.isBot) return
        if (event.emoji != Server.EMOJI_UPVOTE) return
        event.retrieveMessage().queue { message ->
            if (!message.author.isBot) return@queue
            if (message.embeds.isEmpty()) return@queue
            if (message.getEmojiReactionCount(Server.EMOJI_UPVOTE) <= Server.VOTE_COUNTING_BAN_AMOUNT) return@queue
            val userId = message.embeds[0].description?.substringAfter("<@")?.substringBefore(">")?.toLongOrNull() ?: return@queue
            Server.GUILD.retrieveMemberById(userId).queue { member -> Server.GUILD.addRoleToMember(member, Server.COUNTING_BANNED_ROLE) }
            message.editMessageEmbeds(
                embed().setDescription("<@$userId> has been banned from counting.").build()
            ).queue { message.clearReactions().queue() }
        }
    }

}