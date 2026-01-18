package com.learnspigot.bot.newbie

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.iterator

class NewbieTimer {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    init {
        scheduler.scheduleAtFixedRate(
            { CompletableFuture.supplyAsync { checkTimes() } },
            0,
            30,
            TimeUnit.MINUTES
        )
    }

    private fun checkTimes() {
        val current = OffsetDateTime.now()
        for (entry in Registry.NEWBIE.newbies) {
            if (current.isBefore(entry.value.plusDays(3))) continue
            val member = Server.GUILD.getMemberById(entry.key) ?: continue
            Server.GUILD.removeRoleFromMember(member, Server.ROLE_NEWBIE).queue()
            Registry.NEWBIE.newbies.remove(entry.key)
        }
    }

}