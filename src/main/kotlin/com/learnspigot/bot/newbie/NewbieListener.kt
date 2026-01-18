package com.learnspigot.bot.newbie

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.thread.member.ThreadMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class NewbieListener : ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        Server.GUILD.addRoleToMember(event.member, Server.ROLE_NEWBIE).queue()
        Registry.NEWBIE.newbies[event.member.idLong] = event.member.timeJoined
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        Registry.NEWBIE.newbies.remove(event.user.idLong)
    }
}