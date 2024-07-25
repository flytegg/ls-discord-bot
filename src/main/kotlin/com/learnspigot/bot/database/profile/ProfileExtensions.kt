package com.learnspigot.bot.database.profile

import com.learnspigot.bot.Bot
import com.learnspigot.bot.Server
import com.learnspigot.bot.reputation.Reputation
import com.learnspigot.bot.util.InvisibleEmbed
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.Instant

fun Profile.addReputation(fromUserId: String, fromPostId: String, amount: Int) {
    reputation[if (reputation.isEmpty()) 0 else reputation.lastKey() + 1] = Reputation(Instant.now().epochSecond, fromUserId, fromPostId)
    save()

    Bot.jda.getUserById(id)!!.openPrivateChannel().complete().also { privateChannel ->
        privateChannel.sendMessageEmbeds(
            InvisibleEmbed {
                title = "You earned ${if (amount == 1) "" else "$amount "}reputation"
                description = "You gained reputation from <@$fromUserId> in <#$fromPostId>."

                author {
                    name = "You have ${reputation.size} reputation in total"
                }
            }
        ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
    }
}

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