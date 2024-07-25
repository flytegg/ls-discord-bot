package com.learnspigot.bot.database.profile

import com.learnspigot.bot.Server
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

fun Member.addReputation(fromUserId: String, fromPostId: String, amount: Int) = ProfileManager.getProfile(id)
    ?.addReputation(fromUserId, fromPostId, amount)

fun Profile.removeReputation(startId: Int, endId: Int) {
    for (i in startId..endId) {
        reputation.remove(i)
    }

    save()
}

fun Profile.incrementCount(currentCount: Int) {
    totalCounts++
    if (currentCount > highestCount) highestCount = currentCount

    save()
}

fun Profile.fuckedUpCounting() {
    countingFuckUps++
    save()
}

fun User.getProfile() = ProfileManager.getProfile(id)

fun User.giveStudentRole() = Server.guild.addRoleToMember(this, Server.studentRole).queue()
fun Member.giveStudentRole() = user.giveStudentRole()