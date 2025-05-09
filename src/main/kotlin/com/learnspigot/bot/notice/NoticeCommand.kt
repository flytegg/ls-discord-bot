package com.learnspigot.bot.notice;

import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command;
import gg.flyte.neptune.annotation.Description
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button

public class NoticeCommand {

    @Command(
            name = "notice",
            description = "Notices user about something"
    )
    fun onNotice(
        event: SlashCommandInteractionEvent,
        @Description("Notifies selected user") user: User
    ) {
        val userString = "-${user.id}";
        val aiNoticeButton = Button.primary("notice-ai${userString}", "AI Notice")
        val mcUtilsButton = Button.primary("notice-mcutils${userString}", "MCUtils")
        val pastebinButton = Button.primary("notice-pastebin${userString}", "Pastebin")

        event.replyEmbeds(embed()
            .setTitle("What do you want to notice about?")
            .setDescription("Select what has the user done to notice them about it.")
            .build()
        ).setActionRow(
            aiNoticeButton, mcUtilsButton, pastebinButton
        ).queue()
    }
}
