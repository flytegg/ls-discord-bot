package com.learnspigot.bot.notice.types

import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class McUtilsNotice : NoticeType {
    override fun notifyEmbed(userId: String): EmbedBuilder {
        return embed()
            .setTitle("MC Utils")
            .setDescription("" +
                    "[MC Utils](https://mcutils.com/) is a free, community-powered website that offers a wide range of tools for " +
                    "Minecraft players, server admins, and developers — from server jar downloads and start file " +
                    "generators to color text tools, banner creators, and more.")
            .addField("Link", ":right: https://mcutils.com/", false)
            .setColor(Color.CYAN)
    }
}