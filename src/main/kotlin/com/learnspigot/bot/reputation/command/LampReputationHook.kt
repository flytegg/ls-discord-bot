package com.learnspigot.bot.reputation.command

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import revxrsal.commands.autocomplete.SuggestionProvider
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.parameter.ParameterType
import revxrsal.commands.stream.MutableStringStream

class ChannelInput(val channel: MessageChannel)

class ChannelInputResolver : ParameterType<SlashCommandActor, ChannelInput> {
    override fun parse(input: MutableStringStream, context: ExecutionContext<SlashCommandActor>): ChannelInput? {
        val value = input.readString()
        val guild = context.actor().guild()
        val textByName = guild.getTextChannelsByName(value, true).firstOrNull()
        if (textByName != null) return ChannelInput(textByName)
        val thread = guild.threadChannels.firstOrNull { it.name.equals(value, ignoreCase = true) }
        return thread?.let { ChannelInput(it) }
    }

    override fun defaultSuggestions(): SuggestionProvider<SlashCommandActor> = SuggestionProvider { context ->
        val guild = context.actor().guild()
        val textChannels = guild.textChannels.filter{ Server.CATEGORY_CHAT.idLong == it.parentCategoryIdLong }.map { it.name }
        val threadChannels = guild.threadChannelCache.filter { !it.isArchived && !it.isLocked }.map { it.name }
        val input = context.input().peekRemaining().split(" ").last()
        (textChannels + threadChannels).distinct().filter{ it.contains(input, true) }.take(25)
    }
}