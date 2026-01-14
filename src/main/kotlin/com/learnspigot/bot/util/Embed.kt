package com.learnspigot.bot.util

import net.dv8tion.jda.api.EmbedBuilder

fun embed(): EmbedBuilder {
    return EmbedBuilder().setColor(0x2B2D31)
}

fun embed(title: String, description: String) = EmbedBuilder().setColor(0x2B2D31).setTitle(title).setDescription(description).build()
