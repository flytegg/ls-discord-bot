package com.learnspigot.bot.intellijkey

import com.learnspigot.bot.database.profile.ProfileManager
import com.learnspigot.bot.util.embed
import com.learnspigot.bot.util.isStudent
import com.learnspigot.bot.util.replyEphemeral
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Inject
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.OffsetDateTime
import java.time.ZoneOffset

class GetKeyCommand {

    @Inject
    private lateinit var keyRegistry: IJUltimateKeyRegistry

    @Command(
        name = "getkey",
        description = "Unlock your free 6 months IntelliJ Ultimate key",
        permissions = [Permission.MESSAGE_SEND]
    )
    fun onGetKeyCommand(event: SlashCommandInteractionEvent) {
        val member = event.member!!

        if (!member.isStudent) {
            return event.replyEphemeral("You don't have the Student role! You must show you own the course through the verify channel.")
        }

        if (member.timeJoined.isBefore(OffsetDateTime.of(2023, 8, 21, 0, 0, 0, 0, ZoneOffset.UTC))) {
            return event.replyEphemeral("You joined the server before this automated distribution system was added. As such, please DM <@676926873669992459> for your key.")
        }

        val profile = ProfileManager.getProfile(event.user.id)!!
        if (profile.intellijKeyGiven) {
            return event.replyEphemeral("You have already unlocked your free 6 months IntelliJ Ultimate key!")
        }

        val key = keyRegistry.getKey()
            ?: return event.replyEphemeral("Sorry - there are no more keys left to send! Contact a Manager if this is an issue.")

        member.user.openPrivateChannel().complete().let {
            it.sendMessageEmbeds(
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
                event.replyEphemeral("I have privately messaged your key!")
                println("Given key ($key) to ${event.user.effectiveName}.")
            }) {
                keyRegistry.readdKey(key)
                event.replyEphemeral("I am unable to DM you! Please open your DMs so I can privately send your key.")
            }
        }
    }

}