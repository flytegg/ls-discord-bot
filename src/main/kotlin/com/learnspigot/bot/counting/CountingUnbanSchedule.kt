package com.learnspigot.bot.counting

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.Mongo
import java.util.Date
import java.util.Timer
import java.util.TimerTask

object CountingUnbanSchedule {
    fun init() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Mongo.countingBansCollection.find().forEach {
                    val date = it.getDate("date")
                    val dateAfter = date.minutes + Server.COUNTING_BAN_DURATION_DAYS
                    if (dateAfter < Date().minutes) {
                        Server.GUILD.retrieveMemberById(it.getString("userId")).queue { member -> Server.GUILD.removeRoleFromMember(member, Server.ROLE_COUNTING_BANNED).queue() }
                        Mongo.countingBansCollection.deleteOne(it)
                    }
                }
            }
        }, 0, 1 * 60 * 1000)
    }
}