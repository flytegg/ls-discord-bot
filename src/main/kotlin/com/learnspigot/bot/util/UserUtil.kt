package com.learnspigot.bot.util

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

inline val Member.isManager: Boolean get() = roles.contains(Server.managementRole)
inline val Member.isStaff: Boolean get() = roles.contains(Server.staffRole) || isManager
inline val Member.isSupport: Boolean get() = roles.contains(Server.supportRole) || isStaff
inline val Member.isStudent: Boolean get() = roles.contains(Server.studentRole) || isSupport

fun Member.owns(channel: ThreadChannel): Boolean = id == channel.ownerId