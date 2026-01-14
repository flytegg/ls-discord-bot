package com.learnspigot.bot.profile

import com.learnspigot.bot.Server
import com.learnspigot.bot.util.Mongo
import com.learnspigot.bot.util.embed
import com.mongodb.client.model.Filters
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.ErrorResponse

class ProfileListener : ListenerAdapter() {

    override fun onGuildMemberJoin(e: GuildMemberJoinEvent) {
        if (e.guild.id != Server.GUILD_ID) return

        val document = Mongo.userCollection.find(Filters.eq("_id", e.user.id)).first()
        if (document != null && document.containsKey("udemyProfileUrl")) {
            e.user.openPrivateChannel().complete().let {
                it.sendMessageEmbeds(
                    embed()
                        .setTitle("Welcome to the Discord! :tada:")
                        .setDescription(
                            """
                                You have already verified previously so your Student role has been restored.
                                                           
                                *PS: Use [our pastebin](https://paste.learnspigot.com) - pastes never expire!*
                                
                                """.trimIndent()
                        )
                        .build()
                ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
            }

            e.guild.addRoleToMember(e.user, Server.ROLE_STUDENT).queue()
        } else {
            e.user.openPrivateChannel().complete().let {
                it.sendMessageEmbeds(
                    embed()
                        .setTitle("Welcome to the Discord! :tada:")
                        .setDescription(
                            """
                                You have joined the exclusive support community for the [Develop Minecraft Plugins (Java)](https://learnspigot.com) Udemy course.
                                                                
                                :question: Don't have the course? Grab it at <https://learnspigot.com>
                                
                                :thinking: Not convinced? Take a look at what everyone else has to say at <https://vouches.learnspigot.com>
                                                                
                                :star: Have it? Follow the instructions in """.trimIndent() + Server.CHANNEL_VERIFY.asMention + """
                                                       
                                                           
                                *PS: Use [our pastebin](https://paste.learnspigot.com) - pastes never expire!*
                                
                                """.trimIndent()
                        )
                        .setFooter("Without verifying, you can still read the server but won't have access to our 24/7 support team and dozens of tutorials and projects.")
                        .build()
                ).queue(null, ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER) {})
            }
        }
    }

}