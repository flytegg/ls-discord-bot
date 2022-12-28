package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.findGiveaway
import com.learnspigot.bot.LearnSpigotBot.Companion.logger
import com.learnspigot.bot.entity.Giveaway
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.interactions.components.secondary
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import kotlinx.coroutines.*
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import java.time.Instant
import java.util.stream.Collectors
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class GiveawayManager(private val bot: JDA, private val datastore: Datastore) {

    private val activeGiveaways: MutableMap<String, Giveaway> = mutableMapOf()
    private val emoji: Emoji = Emoji.fromUnicode("U+1F389")

    private lateinit var updateJob: Job

    init {
        logger.info("Loading giveaways...")
        datastore.database.getCollection("giveaways").find().forEach { document ->
            val giveaway = datastore.findGiveaway(document.getString("_id"))
            if (giveaway.channelId == "") {
                datastore.delete(giveaway)
                return@forEach
            }
            try {
                bot.getGuildById(System.getenv("GUILD"))!!
                    .getChannel<TextChannel>(giveaway.channelId)!!.retrieveMessageById(giveaway.id).complete()
                activeGiveaways[giveaway.id] = giveaway
            } catch (ex: ErrorResponseException) {
                if (ex.errorCode == 10_008) { // Unknown message error code
                    datastore.delete(giveaway)
                    return@forEach
                }
                throw ex
            }
        }
        bot.listener<ButtonInteractionEvent> {
            if (it.componentId == "enter-giveaway") {
                it.deferReply(true).queue()
                val message = it.message
                if (!activeGiveaways.containsKey(message.id)) return@listener
                val giveaway = activeGiveaways[message.id]!!
                it.hook.editOriginal(
                    "You are now " +
                            "${if (giveaway.entryTrigger(it.user.id)) "**entered**" else "**not entered**"} into the giveaway"
                ).queue()
                val host: User = bot.retrieveUserById(giveaway.host).complete()
                message.editMessageEmbeds(
                    buildEmbed(
                        false, host, giveaway.prize,
                        giveaway.endTime, giveaway.winnerAmount, giveaway.entries.size
                    )
                ).queue()
                datastore.save(giveaway)
            }
        }
        GlobalScope.launch {
            activateJobs()
        }
    }

    private suspend fun activateJobs() = withContext(Dispatchers.Default) {
        updateJob = async {
            while (true) {
                updateGiveaways()
                delay(1.seconds.inWholeMilliseconds)
            }
        }
    }

    fun postGiveaway(host: User, channel: TextChannel, prize: String, time: Long, winnerAmount: Int) {
        val endTime = Instant.now().plusMillis(time).epochSecond
        val message = channel.sendMessageEmbeds(buildEmbed(false, host, prize, endTime, winnerAmount, 0))
            .addActionRow(secondary("enter-giveaway", "", emoji))
            .complete()
        val giveaway = Giveaway(message.id, channel.id, prize, endTime, winnerAmount, host.id)
        activeGiveaways[message.id] = giveaway
        datastore.save(giveaway)
    }

    private fun endGiveaway(giveaway: Giveaway) {
        val winners: MutableList<String> = mutableListOf()
        val message: Message
        try {
            message = bot.getGuildById(System.getenv("GUILD"))!!
                .getChannel<TextChannel>(giveaway.channelId)!!.retrieveMessageById(giveaway.id).complete()
        } catch (ex: ErrorResponseException) {
            if (ex.errorCode == 10_008) { // Unknown message error code
                datastore.delete(giveaway)
                return
            }
            throw ex
        }

        val participants: MutableList<String> = giveaway.entries.toMutableList()
        if (participants.size == 0) {
            message.reply("No one entered the giveaway").queue()
            val host: User = bot.retrieveUserById(giveaway.host).complete()
            message.editMessageEmbeds(
                buildEmbed(
                    true, host, giveaway.prize,
                    giveaway.endTime, giveaway.winnerAmount, giveaway.entries.size, "No winners"
                )
            ).queue()
            return
        } else if (participants.size == 1) {
            winners.add(participants[0])
        } else {
            for (i in 1..giveaway.winnerAmount) {
                val randomWinner = Random.nextInt(0, participants.size)
                winners.add(participants[randomWinner])
            }
        }
        val winnersString = winners.stream().map { id -> bot.retrieveUserById(id).complete().asMention }
            .collect(Collectors.joining(", "))
        val buffer = StringBuffer(winnersString)
        val lastIndex = winnersString.lastIndexOf(",")
        if (lastIndex != -1) {
            buffer.replace(lastIndex, lastIndex + 1, " and")
        }
        val formatedWinnerString = buffer.toString()
        val host: User = bot.retrieveUserById(giveaway.host).complete()
        message.editMessageEmbeds(
            buildEmbed(
                true, host, giveaway.prize,
                giveaway.endTime, giveaway.winnerAmount, giveaway.entries.size, formatedWinnerString
            )
        ).queue()
        message.reply(
            "Congratulations to " +
                    if (winners.size == 1) bot.retrieveUserById(winners[0]).complete().asMention
                    else "$formatedWinnerString for winning the giveaway!"
        ).queue()
        datastore.delete(giveaway)
        activeGiveaways.remove(giveaway.id)

    }

    private fun buildEmbed(
        ended: Boolean,
        host: User,
        prize: String,
        endTime: Long,
        winnerAmount: Int,
        entryAmount: Int,
        winnerString: String = ""
    ): MessageEmbed {
        val embedEmoji: String = if (ended) Emoji.fromUnicode("U+1F4E3").formatted else emoji.formatted
        return Embed {
            title = if (ended) "$embedEmoji Giveaway ended $embedEmoji"
            else "$embedEmoji Giveaway $embedEmoji"
            description = """
            
            Prize: **$prize**
            
            ${if (ended) "Ended: <t:$endTime:R>" else "Ends in: <t:$endTime:R>"} (<t:$endTime:f>)
            Hosted by: ${host.asMention}
            Current entries: **$entryAmount**
            The giveaway has: **$winnerAmount** winner${if (winnerAmount > 1) "s" else ""}
            
            ${if (ended && winnerString != "") "Winners: $winnerString" else ""}
            """
            color = LearnSpigotBot.EMBED_COLOR
        }
    }

    private fun updateGiveaways() {
        val now = Instant.now()
        activeGiveaways.forEach { (_, giveaway) ->
            if (giveaway.channelId == "") activeGiveaways.remove(giveaway.id)
            if (giveaway.endTime <= now.epochSecond) {
                endGiveaway(giveaway)
            }
        }
    }
}