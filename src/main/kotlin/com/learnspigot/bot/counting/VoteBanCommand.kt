package com.learnspigot.bot.counting

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.jda.actor.SlashCommandActor
import java.awt.Color
import java.util.concurrent.TimeUnit

class VoteBanCommand {

    private val cooldowns: Cache<String, Long> = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build()

    @Command("voteban")
    @Description("Vote to ban a user from counting.")
    fun onVoteBanCommand(
        actor: SlashCommandActor,
        @Description("User to vote ban.") user: User
    ) {
        val event = actor.commandEvent()

        val lastUsed = cooldowns.getIfPresent(event.user.id)
        if (lastUsed != null) {
            val remainingTime = 5 - TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastUsed)
            event.replyEmbeds(
                embed().setTitle("Cooldown Active")
                    .setDescription("You can use this command again in $remainingTime minute(s).")
                    .build()
            ).setEphemeral(true).queue()
            return
        }

        if (event.channel.id != Server.CHANNEL_COUNTING.id) {
            event.replyEmbeds(
                embed().setTitle("Unable to use command")
                    .setDescription("This command can only be used in the counting channel.")
                    .setColor(Color.RED)
                    .build()
            ).setEphemeral(true).queue()
        }

        Server.CHANNEL_COUNTING.sendMessageEmbeds(
            embed().setTitle("Vote ban!")
                .setDescription("Should ${user.asMention} be banned from counting?")
                .setFooter("If this message gets more than ${Server.VOTE_COUNTING_BAN_AMOUNT + 1} votes, the user will be banned from counting.")
                .build()
        ).queue { message -> message.addReaction(Server.EMOJI_UPVOTE).queue() }

        event.replyEmbeds(
            embed().setTitle("Voting poll has been created!")
                .build()
        ).setEphemeral(true).queue()

        if (event.member == null) return
        if (!event.member!!.roles.contains(Server.ROLE_MANAGEMENT)) return
        cooldowns.put(event.user.id, System.currentTimeMillis())
    }
}