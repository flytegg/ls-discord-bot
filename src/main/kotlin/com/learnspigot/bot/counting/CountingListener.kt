package com.learnspigot.bot.counting

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.Mongo
import com.mongodb.client.model.Filters
import gg.flyte.neptune.annotation.Inject
import me.superpenguin.mathevaluator.Evaluator
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bson.Document

class CountingListener: ListenerAdapter() {

    @Inject private lateinit var profileRegistry: ProfileRegistry

    private val mongoCollection = Mongo.countingCollection

    var topServerCount: Int = 0
    var serverTotalCounts: Int = 0
    var currentCount = 0

    var lastCount: Message? = null

    init {
        val document = mongoCollection.find().first()
        if (document == null) {
            val newDoc = Document()
            newDoc["highestCount"] = 0
            newDoc["currentCount"] = 0
            newDoc["serverTotalCounts"] = 0
            mongoCollection.insertOne(newDoc)
        } else {
            topServerCount = document.getInteger("highestCount", 0)
            currentCount = document.getInteger("currentCount", 0)
            serverTotalCounts = document.getInteger("serverTotalCounts", 0)
        }
    }

    fun incrementCount(user: User) {
        currentCount++
        serverTotalCounts++
        profileRegistry.findByUser(user).incrementCount(currentCount)
        if (currentCount > topServerCount) topServerCount = currentCount
        val newDoc = mongoCollection.find().first()!!
        newDoc["highestCount"] = topServerCount
        newDoc["currentCount"] = currentCount
        newDoc["serverTotalCounts"] = serverTotalCounts
        mongoCollection.replaceOne(Filters.eq("_id", newDoc.getObjectId("_id")), newDoc)
    }

    fun fuckedUp(user: User) {
        currentCount = 0
        lastCount = null
        profileRegistry.findByUser(user).fuckedUpCounting()
    }

    private fun Channel.isCounting() = id == Environment.get("COUNTING_CHANNEL_ID")
    private fun Message.millisSinceLastCount() = timeCreated.toInstant().toEpochMilli() - (lastCount?.timeCreated?.toInstant()?.toEpochMilli() ?: 0)

    private val thinking = Emoji.fromUnicode("🤔")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isFromGuild || !event.channel.isCounting() || event.guild.id != Server.guildId) return
        if (event.message.attachments.isNotEmpty()) return

        val msg = event.message.contentRaw
        val userId = event.author.id
        if (Evaluator.isValidSyntax(msg)) {
            val evaluated = Evaluator.eval(msg).intValue()
            if (evaluated == currentCount + 1) {
                if (userId.equals(lastCount?.author?.id, true)) return run {
                    event.message.addReaction(Server.downvoteEmoji)
                    event.message.reply("You can't count twice in a row, let someone else join in!").queue()
                    fuckedUp(event.author)
                }
                lastCount = event.message
                event.message.addReaction(Server.upvoteEmoji).queue()
                incrementCount(event.author)
            } else {
                if (evaluated == currentCount && event.message.millisSinceLastCount() < 600) {
                    // ( 600ms delay ) - Arbitrary value based on superficial testing
                    event.message.addReaction(thinking).queue()
                    event.message.reply("I'll let this one slide").queue()
                    return
                }
                val next = currentCount + 1
                fuckedUp(event.author)
                event.message.addReaction(Server.downvoteEmoji).queue()
                event.message.reply("The next number was $next").queue()
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        if (!event.channel.isCounting()) return
        if (event.messageId == lastCount?.id) {
            Server.countingChannel.sendMessage("${lastCount?.author?.asMention} deleted their count of $currentCount").queue()
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        if (!event.channel.isCounting()) return
        if (event.messageId == lastCount?.id) {
            Server.countingChannel.sendMessage("${event.author.asMention} edited their count of $currentCount").queue()
        }
    }

}