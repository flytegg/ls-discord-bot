package com.learnspigot.bot.ai

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

public class AiCommand {

    companion object {
        private const val TITLE = "Artificial Intelligence"
        private const val CONTENT = "" +
                "While artificial intelligence has proven to be a helpful tool across various fields, " +
                "using AI for coding introduces a number of significant concerns that can outweigh the benefits. " +
                "One key issue is its lack of true understanding—AI can produce code that appears correct, " +
                "but without grasping the underlying logic or context, which can lead developers to adopt solutions they don’t fully comprehend, " +
                "making long-term maintenance and debugging more challenging. For these reasons, using AI-generated code is not recommended, " +
                "and support for such code may not be provided or guaranteed."
    }

    @Command(
            name = "ai",
            description = "Tells user not to use ai"
    )
    fun onAiCommandCall(event: SlashCommandInteractionEvent) {
        event.hook.sendMessageEmbeds(embed()
            .setTitle(TITLE)
            .setDescription(CONTENT)
            .setColor(Color.RED)
            .build()
        )
    }

}
