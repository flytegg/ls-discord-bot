package com.learnspigot.bot.starboard

import com.learnspigot.bot.starboard.StarboardUtil.getEmojiReactionCount
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.Server
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed

class StarboardRegistry {
    private val starboardEntries = mutableMapOf<String, StarboardEntry>()

    init {
        Mongo.starboardCollection.find().forEach {
            val starboardMessage = StarboardEntry.fromDocument(it)
            starboardEntries[starboardMessage.originalMessageId] = starboardMessage
        }
    }

    fun removeStarboardEntryAndMessageIfExists(messageId: String) {
        val starboardEntry = starboardEntries[messageId] ?: return
        Mongo.starboardCollection.deleteOne(Filters.eq("originalMessageId", messageId))
        Server.starboardChannel.deleteMessageById(starboardEntry.startboardMessageId).queue {
            starboardEntries.remove(messageId)
        }
    }


    private fun addStarboardEntryAndMessage(message: Message) {
        Server.starboardChannel.sendMessageEmbeds(createStarboardEntryEmbed(message)).queue {
            if (it === null) return@queue
            val starboardEntry = StarboardEntry(message.id, it.id)
            Mongo.starboardCollection.insertOne(starboardEntry.document())
            starboardEntries[message.id] = starboardEntry
        }
    }

    private fun createStarboardEntryEmbed(message: Message): MessageEmbed {
        return embed().apply {
            setAuthor(message.author.name, null, message.author.effectiveAvatarUrl)
            setDescription(message.contentRaw)
            addField("Stars", "⭐️ ${message.getEmojiReactionCount(Server.starEmoji)}", true)
            addField("Original Message", message.jumpUrl, true)
        }.build()
    }

    private fun Message.hasImportantNostarboardEmoji(): Boolean {
        val nostarboardReaction = this.reactions.find {
            it.emoji == Server.nostarboardEmoji
        } ?: return false
        val users = nostarboardReaction.retrieveUsers().complete()
        return users.any {
            if (it.id == this.author.id) return@any true
            val member = Server.guild.getMember(it) ?: return@any false
            return@any member.roles.contains(Server.managerRole)
        }
    }

    fun updateStarboard(message: Message, amount: Int) {
        if (message.hasImportantNostarboardEmoji()) {
            return removeStarboardEntryAndMessageIfExists(message.id)
        }

        val starboardEntry = starboardEntries[message.id]
        if (starboardEntry !== null) {
            if (amount < amountOfStarsNeeded) {
                println("removed message because it exists but too few now")
                return removeStarboardEntryAndMessageIfExists(message.id)
            }
            Server.starboardChannel.editMessageEmbedsById(starboardEntry.startboardMessageId, createStarboardEntryEmbed(message))
                .queue()
            println("edited message because it exists and is >= amount and needs to be updated")
        } else if (amount >= amountOfStarsNeeded) {
            addStarboardEntryAndMessage(message)
            println("added a new message because it doesn't exist but above amount and needs to be added")
        }

        println(starboardEntries)
    }

    fun updateStarboard(message: Message) {
        updateStarboard(message, message.getEmojiReactionCount(Server.starEmoji))
    }


    companion object {
        const val amountOfStarsNeeded = 1
    }
}