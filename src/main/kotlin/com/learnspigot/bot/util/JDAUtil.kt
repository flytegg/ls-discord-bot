package com.learnspigot.bot.util

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

fun Message.getEmojiReactionCount(emoji: Emoji): Int = this.getReaction(emoji)?.count ?: 0

fun ThreadChannel.closeAndLock() = manager.setArchived(true).setLocked(true).queue()

fun IReplyCallback.replyEphemeral(msg: String) = reply(msg).setEphemeral(true).queue()

fun Member.owns(channel: ThreadChannel): Boolean = idLong == channel.ownerIdLong

fun Channel.isChannel(other: Channel) = idLong == other.idLong