package com.learnspigot.bot.notice.types

import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class AiNotice : NoticeType {
    override fun notifyEmbed(userId: String): EmbedBuilder {
        return embed()
            .setTitle("Artificial Intelligence")
            .setDescription("" +
                    "While artificial intelligence has proven to be a helpful tool across various fields, " +
                    "using AI for coding introduces a number of significant concerns that can outweigh the benefits. " +
                    "One key issue is its lack of true understanding—AI can produce code that appears correct, " +
                    "but without grasping the underlying logic or context, which can lead developers to adopt solutions they don’t fully comprehend, " +
                    "making long-term maintenance and debugging more challenging. For these reasons, using AI-generated code is not recommended, " +
                    "and support for such code may not be provided or guaranteed.")
            .setColor(Color.RED)
    }
}