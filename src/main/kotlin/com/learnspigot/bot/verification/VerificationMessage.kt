package com.learnspigot.bot.verification

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.emoji.Emoji

class VerificationMessage {

    init {
        val history = MessageHistory.getHistoryFromBeginning(Server.CHANNEL_VERIFY).complete().retrievedHistory
        if (history.isEmpty())
            Server.CHANNEL_VERIFY.sendMessageEmbeds(
                embed()
                    .setTitle("Welcome to the Plugin Dev Community!")
                    .setDescription(
                        """
                        Since 2018, this exclusive Minecraft plugin development community has offered a warm, welcoming environment with **24/7 help channels** and spaces to **showcase your work**. Once you join, you'll gain access to a **supportive network**, including a **50-man support team** and hundreds of **tutorials and projects**.
                            
                        There are **3 ways** to get access:
                                          
                        :one: Own the [Udemy course](https://learnspigot.com) (click `Udemy` below)
                        :two: Be invited by a friend (click `Friend Code` below)
                        :three: Fill in the form (click `Form` below)
                        
                        This process is to weed out spammers. If you are joining to sell your services, do not bother applying as you will be removed. If you are genuinely interested in learning and growing with our community, we would :heart: to have you.""".trimIndent()
                    )
                    .build()
            )
                .addComponents(
                    ActionRow.of(
                        Button.success("verify_course", "Udemy").withEmoji(Emoji.fromUnicode("\uD83C\uDF93")),
                        Button.success("verify_friend", "Friend Code").withEmoji(Emoji.fromUnicode("\uD83D\uDD11")),
                        Button.success("verify_form", "Form").withEmoji(Emoji.fromUnicode("\uD83D\uDCDD"))
                    )
                )
                .queue()
    }
}