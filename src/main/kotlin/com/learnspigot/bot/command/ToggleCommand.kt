package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand

class ToggleCommand(private val guild: Guild, private val bot: JDA) {
    fun toggleCommand() {
        guild.upsertCommand("toggleping", "Toggle whether or not you want to be pinged when you got a reputation") {
            restrict(guild = true)
            bot.onCommand("toggleping") {
                val role = guild.getRoleById(System.getenv("PING_ID"))
                if (it.member!!.roles.contains(role)) {
                    guild.removeRoleFromMember(it.member!!, role!!).queue()
                    it.replyEmbed({
                        title = "Success"
                        description = "You will no longer be pinged when you got a reputation"
                        color = 0xff0000
                    }).queue()
                } else {
                    // add role to the user
                    guild.addRoleToMember(it.member!!, role!!).queue()
                    it.replyEmbed({
                        title = "Success"
                        description = "You will be pinged when you got a reputation"
                        color = 0x00ff00
                    }).queue()
                }

            }
        }.queue()
    }
}
