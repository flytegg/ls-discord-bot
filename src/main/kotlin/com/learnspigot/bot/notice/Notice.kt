package com.learnspigot.bot.notice

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

enum class Notice(val reply: ReplyCallbackAction.(targetUserId: Long) -> Unit, val description: String, val helpPostOnly: Boolean) {
    CLOSE({ targetUserId ->
        val closeId = Server.GUILD.retrieveCommands().complete().firstOrNull { it.name == "close" }?.id
        addEmbeds(embed()
            .setTitle("Issue Fixed?")
            .setDescription(" ")
            .addField(
                "Looks like your post issue is fixed",
                "Use ${if (closeId == null) "/close" else "</close:$closeId>"} and select the helpers.",
                false
            )
            .build()
        )
        setContent("<@$targetUserId> - Have you resolved your issue?")
    }, "Reminds someone to close the post.", true),
    HELP({ targetUserId ->
        addEmbeds(embed()
            .setTitle("Looking for assistance?")
            .setDescription("Please make a post in ${Server.CHANNEL_HELP.jumpUrl} so we can help.")
            .build()
        )
        setContent("<@$targetUserId> - Stuck on an issue?")
    }, "Informs people to use the help channel.", false),
    PING({ targetUserId ->
        val closeId = Server.GUILD.retrieveCommands().complete().firstOrNull { it.name == "close" }?.id
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