package com.learnspigot.bot.profile

import com.learnspigot.bot.Registry
import com.learnspigot.bot.Server
import com.learnspigot.bot.util.embed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Named
import revxrsal.commands.jda.actor.SlashCommandActor
import revxrsal.commands.jda.annotation.CommandPermission
import java.awt.Color
import java.time.Instant

class TransferCommand {

    @Command("transfer")
    @Description("Transfer all of a user's data and roles to their new account")
    @CommandPermission(Permission.MANAGE_ROLES)
    fun onTransferCommand(
        actor: SlashCommandActor,
        @Description("The user's old account (paste the ID if they have left)") @Named("old-account") oldUser: User,
        @Description("The user's new account") @Named("new-account") newUser: User
    ) {
        val event = actor.commandEvent()

        fun fail(reason: String) = event.replyEmbeds(
            embed().setTitle("Unable to transfer").setDescription(reason).setColor(Color.RED).build()
        ).setEphemeral(true).queue()

        if (oldUser.id == newUser.id) return fail("The old and new account are the same.")
        if (newUser.isBot) return fail("You can't transfer to a bot.")
        val newMember = Server.GUILD.getMemberById(newUser.id) ?: return fail("${newUser.asMention} isn't in the server.")
        val oldProfile = Registry.PROFILES.findById(oldUser.id) ?: return fail("${oldUser.asMention} has no data to transfer.")

        val oldMember = Server.GUILD.getMemberById(oldUser.id)
        val newProfile = Registry.PROFILES.findByUser(newUser)
        Registry.PROFILES.transfer(oldProfile, newProfile)

        // Copy over any roles the bot is able to give. If the old account has left, they at least get Student back.
        val roles = (oldMember?.roles ?: listOf(Server.ROLE_STUDENT))
            .filter { !it.isManaged && Server.GUILD.selfMember.canInteract(it) }
            .toMutableSet()
        val banExpiry = newProfile.countingBanExpiry
        if (banExpiry != null && banExpiry > Instant.now().epochSecond) roles.add(Server.ROLE_COUNTING_BANNED)
        roles.forEach { Server.GUILD.addRoleToMember(newMember, it).queue() }

        println("[Transfer] '${event.user.name}' transferred '${oldUser.name}' (${oldUser.id}) to '${newUser.name}' (${newUser.id})")
        Server.CHANNEL_GENERAL.sendMessage("Welcome ${newUser.asMention} to the community! (account transfer from ${oldUser.asMention})").queue()

        event.replyEmbeds(
            embed()
                .setTitle("Operation successful")
                .setDescription(
                    "Transferred ${oldUser.name} (${oldUser.asMention}) to ${newUser.name} (${newUser.asMention})\n\n" +
                    "${newProfile.reputation.size} reputation points, ${roles.size} role${if (roles.size == 1) "" else "s"} given"
                )
                .build()
        ).setEphemeral(true).queue()
    }

}
