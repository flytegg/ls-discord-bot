package com.learnspigot.bot.help.notice

import com.learnspigot.bot.Server

enum class Notice(private val message: String, val helpPostOnly: Boolean) {
    CLOSE("<@user_id> if your issue has been resolved, please run /close and _only_ select **people who helped**.", true),
    HELP("<@user_id> if you need any help, please make a post in ${Server.helpChannel.jumpUrl}.", false);

    fun message(targetUserId: Long): String {
        var notice = message
        notice = notice.replace("user_id", targetUserId.toString())
        return notice
    }

    fun rawMessage(): String = message
}