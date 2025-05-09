package com.learnspigot.bot.notice.types

import net.dv8tion.jda.api.EmbedBuilder

interface NoticeType {

    public fun notifyEmbed(userId: String): EmbedBuilder

}