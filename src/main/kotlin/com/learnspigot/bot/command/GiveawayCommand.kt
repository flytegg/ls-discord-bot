package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.manager.GiveawayManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.replyModal
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import kotlin.time.Duration.Companion.hours

class GiveawayCommand(private val guild: Guild, private val bot: JDA, private val giveawayManager: GiveawayManager) {

    init {
        guild.upsertCommand("giveaway", "Create a giveaway") {
            restrict(true, DefaultMemberPermissions.DISABLED)
            option<TextChannel>("channel", "The channel you would like to make the giveaway in", true)

            bot.onCommand("giveaway") {
                it.replyModal("giveaway-${it.getOption("channel")!!.asChannel.id}", "Create Giveaway") {
                    paragraph("item", "What is given away", true)
                    short("time", "Time", true, placeholder = "The amount of hours the giveaway should run for")
                    short("winner-amount", "The amount of winner", true)
                }.queue()
            }
        }.queue()
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("giveaway-")) {
                val channel = it.guild!!.getTextChannelById(it.modalId.split(Regex.fromLiteral("-"))[1])!!
                val item = it.getValue("item")!!.asString
                val winnerAmount: Int = it.getValue("winner-amount")!!.asString.toInt()

                val rawTime: Double = it.getValue("time")!!.asString.toDoubleOrNull() ?: run {
                    it.replyEmbed({
                        title = "Error!"
                        description = "The time you specified is not a number"
                    }).queue()
                    return@listener
                }

                if(rawTime <= 0) {
                    it.replyEmbed({
                        title = "Error!"
                        description = "The time you specified is negative. We haven't built a time machine (yet!)."
                    }, ephemeral = true).queue()
                    return@listener
                }

                val time = rawTime.hours.inWholeMilliseconds
                giveawayManager.postGiveaway(it.user ,channel, item, time, winnerAmount)
                it.deferEdit().queue()
            }
        }
    }
}
