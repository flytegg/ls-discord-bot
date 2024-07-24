package com.learnspigot.bot.help

import com.learnspigot.bot.Bot.jda
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.InvisibleEmbed
import com.learnspigot.bot.util.embed
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.messages.Embed
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object PasteCommand {

    init {
        jda.onCommand("pastebin") { event ->
            event.replyEmbeds(getNewPasteBinEmbed).queue()
        }
    }

    val getNewPasteBinEmbed = InvisibleEmbed {
        title = "LearnSpigot Pastebin"
        description = "${Server.rightEmoji.asMention} https://paste.learnspigot.com/"

        field {
            name = "How do I use this?"
            value =
                "Copy paste your code/error directly from your IDE/console, save it and share the link from the search bar into this chat so we can help."
            inline = false
        }

        field {
            name = "Important notes:"
            value =
                "When sharing code with an error, send the identical class without any changes. Use only one class in each pastebin."
            inline = true
        }
    }
}
