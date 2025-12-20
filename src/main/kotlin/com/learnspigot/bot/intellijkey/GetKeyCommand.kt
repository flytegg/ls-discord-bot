package com.learnspigot.bot.intellijkey

import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.OffsetDateTime
import java.time.ZoneOffset

class GetKeyCommand {

    @Inject
    private lateinit var profileRegistry: ProfileRegistry

    @Inject
    private lateinit var keyRegistry: IJUltimateKeyRegistry

    @Command(
        name = "getkey",
        description = "Unlock your free 6 months IntelliJ Ultimate key",
        permissions = [Permission.MESSAGE_SEND]
    )
    fun onGetKeyCommand(event: SlashCommandInteractionEvent) {
        // First, defer the reply to prevent timeout
        event.deferReply(true).queue() // 'true' makes the reply ephemeral

        val member = event.member!!
        val isManager = member.roles.contains(Server.managementRole)

        if (!member.roles.contains(event.jda.getRoleById(Environment.get("STUDENT_ROLE_ID")))) {
            event.hook.sendMessage("You don't have the Student role! You must show you own the course through the verify channel.").queue()
            return
        }

        if (!isManager && member.timeJoined.isBefore(OffsetDateTime.of(2023, 8, 21, 0, 0, 0, 0, ZoneOffset.UTC))) {
            event.hook.sendMessage("You joined the server before this automated distribution system was added. As such, please DM <@676926873669992459> for your key.").queue()
            return
        }

        val profile = profileRegistry.findByUser(event.user)
        if (!isManager && profile.intellijKeyGiven) {
            event.hook.sendMessage("You have already unlocked your free 6 months IntelliJ Ultimate key!").queue()
            return
        }

        val key = keyRegistry.getKey()
        if (key == null) {
            event.hook.sendMessage("Sorry - there are no more keys left to send! Contact a Manager if this is an issue.").queue()
            return
        }

        member.user.openPrivateChannel().queue({ channel ->
            channel.sendMessageEmbeds(
                embed()
                    .setTitle("IntelliJ Ultimate Key")
                    .setDescription("""
                    Thanks to our partnership with our friends over at JetBrains, as a free perk for buying the course you receive a 6 months IntelliJ Ultimate license!
                                            
                    Your key: $key
                    Redeem @ <https://www.jetbrains.com/store/redeem>

                    Note: IntelliJ Community version is free and used throughout the course. This key is to unlock the Ultimate version, which is loaded with extra features.
                    """)
                    .setFooter("PS: If you ever need help, come use the #help channel in the server.")
                    .build()
            ).queue({
                profile.apply {
                    intellijKeyGiven = true
                    save()
                }

                keyRegistry.removeKeyFromFile(key)

                event.hook.sendMessage("I have privately messaged your key!").queue()

                val logChannel = event.jda.getTextChannelById(Environment.get("KEYLOG_CHANNEL_ID"))
                logChannel?.sendMessageEmbeds(
                    embed()
                        .setTitle("Key Given")
                        .setDescription("Given IntelliJ Ultimate key to ${event.user.asMention}")
                        .addField("Key", key, false)
                        .setTimestamp(OffsetDateTime.now())
                        .build()
                )?.queue()
            }) {
                keyRegistry.readdKey(key)
                event.hook.sendMessage("I am unable to DM you! Please open your DMs so I can privately send your key.").queue()
            }
        }, {
            keyRegistry.readdKey(key)
            event.hook.sendMessage("I am unable to open a DM channel with you. Please check your privacy settings and try again.").queue()
        })
    }

}
