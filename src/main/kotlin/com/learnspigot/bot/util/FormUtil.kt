package com.learnspigot.bot.util

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

fun ThreadChannel.closeAndLock() = manager.setArchived(true).setLocked(true).queue()