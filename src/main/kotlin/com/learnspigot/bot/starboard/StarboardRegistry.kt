package com.learnspigot.bot.starboard

import com.learnspigot.bot.Environment
import com.learnspigot.bot.starboard.StarboardUtil.getEmojiReactionCount
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User

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
        Server.starboardChannel.sendMessageEmbeds(createStarboardEntryEmbed(message, false)).queue {
            if (it === null) return@queue
            val starboardEntry = StarboardEntry(message.id, it.id)
            Mongo.starboardCollection.insertOne(starboardEntry.document())
            starboardEntries[message.id] = starboardEntry
        }
    }

    private fun createStarboardEntryEmbed(message: Message, edited: Boolean): MessageEmbed {
        return embed().apply {
            setAuthor(message.author.name, null, message.author.effectiveAvatarUrl)
            setDescription(message.contentRaw)
            addField("Stars", "⭐️ ${message.getEmojiReactionCount(Server.starEmoji)}", true)
            addField("Original Message", message.jumpUrl, true)
            addField("Was Edited", if (edited) "Yes" else "No", true)
            if (message.attachments.isNotEmpty()) setImage(message.attachments.first().proxyUrl)
        }.build()
    }

    private fun Message.hasImportantNostarboardEmoji(): Boolean {
        val nostarboardReaction = this.reactions.find {
            it.emoji == Server.nostarboardEmoji
        } ?: return false
        val users = nostarboardReaction.retrieveUsers().complete()
        return usersAreAuthorOrManagement(users, this.author)
    }

    private fun usersAreAuthorOrManagement(users: List<User>, author: User): Boolean {
        return users.any {
            if (it.id == author.id) return@any true
            val member = Server.guild.getMember(it) ?: return@any false
            return@any member.roles.contains(Server.managementRole)
        }
    }

    fun updateNostarboard(message: Message) {
        if (message.getEmojiReactionCount(Server.nostarboardEmoji) >= 1) {
            val nostarboardReaction = message.getReaction(Server.nostarboardEmoji)
            val users = nostarboardReaction?.retrieveUsers()?.complete()

            val hasImportantNostarboardEmoji = usersAreAuthorOrManagement(users ?: listOf(), message.author)
            if (hasImportantNostarboardEmoji) {
                removeStarboardEntryAndMessageIfExists(message.id)
            }

            users?.forEach {
                val member = Server.guild.getMember(it)
                if (it.id == message.author.id || member?.roles?.contains(Server.managementRole) == true) return@forEach
                nostarboardReaction.removeReaction(it).queue()
            }
        } else {
            updateStarboard(message, false)
        }
    }

    fun updateStarboard(message: Message, amount: Int, edited: Boolean = false) {
        println(message.getEmojiReactionCount(Server.starEmoji))

        if (message.hasImportantNostarboardEmoji()) return updateNostarboard(message)

        val starboardEntry = starboardEntries[message.id]
        if (starboardEntry !== null) {
            if (amount < amountOfStarsNeeded) {
                return removeStarboardEntryAndMessageIfExists(message.id)
            }
            Server.starboardChannel.editMessageEmbedsById(
                starboardEntry.startboardMessageId, createStarboardEntryEmbed(message, edited)
            ).queue()
        } else if (amount >= amountOfStarsNeeded) {
            addStarboardEntryAndMessage(message)
        }

        println(starboardEntries)
    }

    fun updateStarboard(message: Message) {
        updateStarboard(message, message.getEmojiReactionCount(Server.starEmoji))
    }

    fun updateStarboard(message: Message, edited: Boolean) {
        updateStarboard(message, message.getEmojiReactionCount(Server.starEmoji), edited)
    }


    companion object {
        val amountOfStarsNeeded: Int = Environment.get("STARBOARD_AMOUNT").toInt()
    }
}