package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.editEmbed
import com.learnspigot.bot.manager.PollManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.interactions.components.replyModal
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import kotlin.time.Duration.Companion.hours

class PollCommand(private val bot: JDA, private val pollManager: PollManager) {

    init {
        bot.upsertCommand("poll", "Creates a poll") {
            restrict(true, DefaultMemberPermissions.DISABLED)
            option<TextChannel>("channel", "The channel you would like to send the poll in", true)
            option<Int>("options", "The amount of options", true)

            bot.onCommand("poll") {
                it.replyModal("poll-${it.getOption("channel")!!.asChannel.id}", "Create Poll") {
                    paragraph("question", "The question", true)
                    short("time", "Time", true, placeholder = "The amount of hours the poll should run for")
                    repeat(it.getOption("options")!!.asInt) { i  ->
                        short("option-$i", "Emoji for option ${i + 1}")
                    }
                }.queue()
            }
        }.queue()
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("poll-")) {
                it.deferReply(true).queue()
                val channel = it.guild!!.getTextChannelById(it.modalId.split(Regex.fromLiteral("-"))[1])!!
                val question = it.getValue("question")!!.asString

                if(it.values.filter { it.id.startsWith("option-") }.count()
                    != it.values.filter { it.id.startsWith("option-") }.map { it.asString }.distinct().count()) {
                    it.editEmbed({
                        title = "Error!"
                        description = "You have put one emoji in more than once."
                    })
                    return@listener
                }

                val options: List<Emoji> = it.values
                    .filter { it.id.startsWith("option-") }
                    .map { Emoji.fromFormatted(it.asString) }


                val rawTime: Double = it.getValue("time")!!.asString.toDoubleOrNull() ?: run {
                    it.editEmbed({
                        title = "Error!"
                        description = "The time you specified is not a number"
                    }).queue()
                    return@listener
                }
                if(rawTime <= 0) {
                    it.editEmbed({
                        title = "Error!"
                        description = "The time you specified is negative. We haven't built a time machine (yet!)."
                    }).queue()
                    return@listener
                }

                val time = rawTime.hours.inWholeMilliseconds
                pollManager.postPoll(channel, question, time, options)
            }
            it.deferEdit().queue()
        }
    }
}