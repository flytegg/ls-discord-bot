package com.learnspigot.bot.starboard

import com.learnspigot.bot.Environment
import com.learnspigot.bot.starboard.StarboardUtil.getEmojiReactionCount
import com.learnspigot.bot.database.Mongo
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

    fun removeStarboardEntry(messageId: String) {
        val starboardEntry = starboardEntries[messageId] ?: return

        Mongo.starboardCollection.deleteOne(Filters.eq("originalMessageId", messageId))
        Server.starboardChannel.deleteMessageById(starboardEntry.startboardMessageId).queue {
            starboardEntries.remove(messageId)
        }
    }


    private fun addStarboardEntry(message: Message) {
        Server.starboardChannel.sendMessageEmbeds(createStarboardEntryEmbed(message, message.isEdited)).queue {
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
            setFooter(if (edited) "This message has been edited." else "")
            if (message.attachments.isNotEmpty()) setImage(message.attachments.first().proxyUrl)
        }.build()
    }

    private fun Message.hasNoStarboardEmoji(): Boolean {
        val noStarboardReaction = this.reactions.find {
            it.emoji == Server.nostarboardEmoji
        } ?: return false

        val users = noStarboardReaction.retrieveUsers().complete()
        return usersAreAuthorOrManagement(users, this.author)
    }

    private fun usersAreAuthorOrManagement(users: List<User>, author: User): Boolean {
        return users.any {
            if (it.id == author.id) return@any true
            val member = Server.guild.getMember(it) ?: return@any false
            return@any member.roles.contains(Server.managementRole)
        }
    }

    fun updateNoStarboard(message: Message) {
        if (message.getEmojiReactionCount(Server.nostarboardEmoji) >= 1) {
            val noStarboardReaction = message.getReaction(Server.nostarboardEmoji)
            val users = noStarboardReaction?.retrieveUsers()?.complete()

            val hasNoStarboardEmoji = usersAreAuthorOrManagement(users ?: listOf(), message.author)
            if (hasNoStarboardEmoji) {
                removeStarboardEntry(message.id)
            }

            users?.forEach {
                val member = Server.guild.getMember(it)
                if (it.id == message.author.id || member?.roles?.contains(Server.managementRole) == true) return@forEach
                noStarboardReaction.removeReaction(it).queue()
            }
        } else {
            updateStarboard(message, false)
        }
    }

    fun updateStarboard(message: Message, amount: Int, edited: Boolean = false) {
        if (message.hasNoStarboardEmoji()) return updateNoStarboard(message)

        val starboardEntry = starboardEntries[message.id]
        if (starboardEntry !== null) {
            if (amount < amountOfStarsNeeded) {
                return removeStarboardEntry(message.id)
            }
            Server.starboardChannel.editMessageEmbedsById(
                starboardEntry.startboardMessageId, createStarboardEntryEmbed(message, edited)
            ).queue()
        } else if (amount >= amountOfStarsNeeded) {
            addStarboardEntry(message)
        }
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
