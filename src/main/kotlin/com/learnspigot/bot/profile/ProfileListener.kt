package com.learnspigot.bot.profile

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.ErrorResponse

class ProfileListener : ListenerAdapter() {

    override fun onGuildMemberJoin(e: GuildMemberJoinEvent) {
        if (e.guild.id != Server.guildId) return

        e.user.openPrivateChannel().complete().let {
            it.sendMessageEmbeds(
                embed()
                    .setTitle("Welcome to the Discord! :tada:")
                    .setDescription(
                        """
                                You have joined the exclusive support community for the [Develop Minecraft Plugins (Java)](https://learnspigot.com) Udemy course.
                                                                
                                :question: Don't have the course? Grab it at [https://learnspigot.com](https://learnspigot.com)
                                
                                :thinking: Not convinced? Take a look at what everyone else has to say at [https://vouches.learnspigot.com](https://vouches.learnspigot.com)
                                                                
                                :star: Have it? Follow the instructions in """.trimIndent() + e.guild.getTextChannelById(Environment.get("VERIFY_CHANNEL_ID"))!!.asMention + """
                                                           
                                *PS: Use [our pastebin](https://paste.learnspigot.com) - pastes never expire!*
                                
                                """.trimIndent()
                    )
                    .setFooter("Without verifying, you can still read the server but won't have access to our 24/7 support team and dozens of tutorials and projects.")
                    .build()
            ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
        }
    }

}