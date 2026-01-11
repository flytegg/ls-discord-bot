package com.learnspigot.bot.help.notice

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

enum class Notice(val reply: ReplyCallbackAction.(targetUserId: Long) -> Unit, val description: String, val helpPostOnly: Boolean) {
    CLOSE({ targetUserId ->
        setContent("<@$targetUserId> if your issue has been resolved, please run /close and _only_ select **people who helped**.")
    }, "Reminds someone to close the post.", true),
    HELP({ targetUserId ->
        setContent("<@$targetUserId> if you need any help, please make a post in ${Server.helpChannel.jumpUrl}.")
    }, "Informs people to use the help channel.", false),
    PING({ targetUserId ->
        val closeId = Server.guild.retrieveCommands().complete().firstOrNull { it.name == "close" }?.id
        addEmbeds(embed()
            .setTitle("Are there any updates?")
            .setDescription(" ")
            .addField(
                "I have new code/error",
                "Paste it @ https://paste.learnspigot.com and send it so we can help.",
                false
            )
            .addField("I figured it out", "Great job! Run ${if (closeId == null) "/close" else "</close:$closeId>"} and select contributors.", false)
            .build()
        )
        setContent("<@$targetUserId> - You haven't responded in a while!")
    }, "Pings the target user asking if their issue has been resolved.", true);
}