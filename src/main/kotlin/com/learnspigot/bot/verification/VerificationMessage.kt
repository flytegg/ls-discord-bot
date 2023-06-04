package com.learnspigot.bot.verification

import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.util.function.Consumer

class VerificationMessage(guild: Guild) {

    init {
        guild.getTextChannelById(System.getenv("VERIFY_CHANNEL_ID"))!!.sendMessageEmbeds(
            embed()
                .setTitle("VERIFY YOU OWN THE COURSE")
                .setDescription("""
                                Welcome to the Discord for the LearnSpigot course!
                                                                
                                :disappointed: **Don't own the course? See """.trimIndent() + guild.getTextChannelById(System.getenv("GET_COURSE_CHANNEL_ID"))!!.asMention + """
                                **
                                                        
                                The URL you need to use is the link to your public profile, to get this:
                                :one: Hover over your profile picture in the top right on Udemy
                                :two: Select "Public profile" from the dropdown menu
                                :three: Copy the link from your browser
                                                                
                                Please make sure that you have [privacy settings](https://www.udemy.com/instructor/profile/privacy/) enabled so that we can verify you own the course.""".trimIndent())
                .setFooter("Once you've verified, you'll have access to our 50 man support team, 50+ additional tutorials and a supportive community.")
                .build())
            .addActionRow(Button.success("verify", "Click to Verify"))
            .queue()
    }

}
