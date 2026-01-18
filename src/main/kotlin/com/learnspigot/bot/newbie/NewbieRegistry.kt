package com.learnspigot.bot.newbie

import com.learnspigot.bot.Server
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

class NewbieRegistry {

    val newbies: HashMap<Long, OffsetDateTime> = hashMapOf()

    init {
        CompletableFuture.supplyAsync {
            for (member in Server.GUILD.findMembersWithRoles(Server.ROLE_NEWBIE).get()) {
                newbies[member.idLong] = member.timeJoined
            }
        }
    }

}