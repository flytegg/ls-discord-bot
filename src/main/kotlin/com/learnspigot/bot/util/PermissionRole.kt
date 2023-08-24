package com.learnspigot.bot.util

import net.dv8tion.jda.api.Permission

// Maps Discord permission to Role which has perm on server, accounting for Discord making command access permission based and not role based
// These need implemented in commands when somebody can be bothered, and also updated to the relevant perm
object PermissionRole {

    val STUDENT = Permission.MESSAGE_SEND
    val TRIAL_HELPER = Permission.MESSAGE_SEND
    val HELPER = Permission.MESSAGE_SEND
    val SUPPORT = Permission.MESSAGE_SEND
    val SPECIALIST = Permission.MESSAGE_SEND
    val EXPERT = Permission.PRIORITY_SPEAKER
    val MANAGER = Permission.MESSAGE_SEND

}