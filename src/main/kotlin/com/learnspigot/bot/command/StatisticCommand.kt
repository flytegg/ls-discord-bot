package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.UserProfile
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.morphia.Datastore
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.text.SimpleDateFormat

// TODO: Export to html
class StatisticCommand(guild: Guild, bot: JDA, datastore: Datastore) {

    private val dateFormat = SimpleDateFormat("dd/MM/yy")

    init {
        guild.upsertCommand("stats", "View user statistics") {
            restrict(true, Permission.MESSAGE_MANAGE)
            option<Member>("user", "The user you wish to search", true)

            option<TextChannel>("channel", "Limit to a certain channel")
            option<String>("keywords", "Limit to keywords. Separate with ,")
            option<String>("range_start", "Limit to after a date. (Format: dd/MM/yy)")
            option<String>("range_end", "Limit to before a date. (Format: dd/MM/yy)")

            bot.onCommand("stats") {
                it.deferReply().queue()
                val target = it.getOption("user")?.asMember!!
                val profile: UserProfile = datastore.findUserProfile(target.id)

                val channel = it.getOption("channel")?.asChannel
                val keywordsRaw = it.getOption("keywords")?.asString
                val rangeStart = it.getOption("range_start")?.asString
                val rangeEnd = it.getOption("range_end")?.asString

                it.replyEmbed({
                    title = "Message Statistics"
                    description = "Look at ${target.asMention}'s statistics for the below criteria"
                    thumbnail = target.effectiveAvatarUrl

                    var history = profile.messageHistory.toList()

                    if(channel != null) {
                        history = history.filter { it.channelId == channel.id }
                        field("Channel", channel.asMention, false)
                    }
                    if(keywordsRaw != null) {
                        val keywords = keywordsRaw.split(", ", ",")
                        history = history.filter {
                            keywords.forEach { kw ->
                                if(it.content.contains(Regex.fromLiteral(kw))) return@filter true
                            }
                            false
                        }
                        field {
                            name = "Keywords"
                            keywords.forEach {
                                value += "\u2022 $it\n"
                            }
                            inline = false
                        }
                    }
                    if(rangeStart != null) {
                        val timestamp = dateFormat.parse(rangeStart).time
                        history = history.filter { it.timestamp >= timestamp }
                        field("Range Start", rangeStart, false)
                    }
                    if(rangeEnd != null) {
                        val timestamp = dateFormat.parse(rangeEnd).time
                        history = history.filter { it.timestamp <= timestamp }
                        field("Range Start", rangeEnd, false)
                    }

                    field("Message count", history.size.toString(), false)
                }).queue()
            }
        }.queue()
    }
}