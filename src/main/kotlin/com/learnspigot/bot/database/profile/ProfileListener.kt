package com.learnspigot.bot.database.profile

import com.learnspigot.bot.Bot.jda
import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.InvisibleEmbed
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse

class ProfileListener {

    init {
        jda.listener<GuildMemberJoinEvent> { event ->
            val existingUser = event.user.getProfile()

            if (existingUser == null) {
                event.user.openPrivateChannel().complete().also { privateChannel ->
                    privateChannel.sendMessageEmbeds(
                        InvisibleEmbed {
                            title = "Welcome to the Discord! :tada:"
                            description = """
                                    You have joined the exclusive support community for the [Develop Minecraft Plugins (Java)](https://learnspigot.com) Udemy course.
                                    
                                    :question: Don't have the course? Grab it at <https://learnspigot.com>
                                    
                                    :thinking: Not convinced? Take a look at what everyone else has to say at <https://vouches.learnspigot.com>
                                    
                                    :star: Have it? Follow the instructions in ${Server.verifyChannel.asMention}
                                    
                                    
                                    *PS: Use [our pastebin](https://paste.learnspigot.com) - pastes never expire!*
                           
                                """.trimIndent()

                            footer("Without verifying, you can still read the server but won't have access to our 24/7 support team and dozens of tutorials and projects.")
                        }
                    ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
                }
            }
        }
    }

}


