package com.learnspigot.bot.verification

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.MessageHistory

class VerificationMessage {

    init {
        val history = MessageHistory.getHistoryFromBeginning(Server.CHANNEL_VERIFY).complete().retrievedHistory
        if (history.isEmpty())
            Server.CHANNEL_VERIFY.sendMessageEmbeds(
                embed()
                    .setTitle("VERIFY YOU OWN THE COURSE")
                    .setDescription(
                        """
                        Welcome to the Discord for the LearnSpigot course!
                                                    
                        :disappointed: **Don't own the course? See """.trimIndent() + Server.CHANNEL_GET_COURSE.asMention + """
                        **
                                            
                        The URL you need to use is the link to your public profile, to get this:
                        :one: Hover over your profile picture in the top right on Udemy
                        :two: Select "Public profile" from the dropdown menu
                        :three: Copy the link from your browser
                                                    
                        Please make sure that you have [privacy settings](https://www.udemy.com/instructor/profile/privacy/) enabled so that we can verify you own the course.
                        
                        **On Udemy Personal Plan or Udemy For Business?** When verifying, indicate this by typing "Yes" in the provided field. (If you purchased the course directly and don't know what these are, simply answer "No")""".trimIndent()
                    )
                    .setFooter("Once you've verified, you'll have access to our 50-person support team, hundreds of additional tutorials, and a supportive community.")
                    .build()
            )
                .addComponents(ActionRow.of(Button.success("verify", "Click to Verify")))
                .queue()
    }
}