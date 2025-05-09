package com.learnspigot.bot.notice.types

import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class PastebinNotice : NoticeType {
    override fun notifyEmbed(userId: String): EmbedBuilder {
        return embed()
            .setTitle("Pastebin")
            .setDescription("" +
                    "Because Discord tends to break code formatting in text channels, we recommend " +
                    "using our own Pastebin site when sharing code. It helps keep things clear and makes it " +
                    "easier for us to understand and solve the problem with you.")
            .addField("Link", ":right: https://paste.learnspigot.com/", false)
            .setColor(Color.YELLOW)
    }
}