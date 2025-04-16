package com.learnspigot.bot.verification

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.interactions.components.buttons.Button

class VerificationMessage(guild: Guild) {

    init {
        val history = MessageHistory.getHistoryFromBeginning(Server.verifyChannel).complete().retrievedHistory
        if (history.isEmpty())
            Server.verifyChannel.sendMessageEmbeds(
                embed()
                    .setTitle("VERIFY YOU OWN THE COURSE")
                    .setDescription(
                        """
                        Welcome to the Discord for the LearnSpigot course!
                                                    
                        :disappointed: **Don't own the course? See """.trimIndent() + guild.getTextChannelById(
                            Environment.get("GET_COURSE_CHANNEL_ID")
                        )!!.asMention + """
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
                .addActionRow(Button.success("verify", "Click to Verify"))
                .queue()
    }
}