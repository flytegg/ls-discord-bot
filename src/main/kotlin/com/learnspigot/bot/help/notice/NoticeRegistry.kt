package com.learnspigot.bot.help.notice

import com.learnspigot.bot.Server
import kotlin.collections.iterator

class NoticeRegistry {

    private val notices = hashMapOf<String, String>() // key to notice

    fun getNotice(key: String, pairs: Map<String, String>): String? = notices[key]?.let { notice ->
        var newNotice = notice
        for (pair in pairs) newNotice = newNotice.replace(pair.key, pair.value)
        newNotice
    }

    fun notices(): Map<String, String> = notices

    init {
        notices["close"] = """
            <@user_id> if your issue has been resolved, please run /close and _only_ select **people who helped**.
        """.trimIndent()

        notices["help"] = """
            <@user_id> if you need any help, please make a post in ${Server.helpChannel.jumpUrl}.
        """.trimIndent()
    }

}