package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.LearnSpigotBot.Companion.findOne
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.LearnSpigotBot.Companion.replyEmbed
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.http.UdemyService
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.morphia.Datastore
import dev.morphia.query.filters.Filters
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member

class ProfileCommand(guild: Guild, bot: JDA, datastore: Datastore) {

    init {
        guild.upsertCommand("profile", "View a user's profile") {
            restrict(guild = true, perm = Permission.MANAGE_ROLES)
            option<Member>("member", "Look up  profile by member", required = false)
            option<String>("udemy_url", "Look up profile by udemy url", required = false)

            bot.onCommand("profile") {
                val profile: UserProfile = if(it.getOption("member") != null) {
                    val member = it.getOption("member")?.asMember!!
                    datastore.findUserProfile(member.id)
                }else if(it.getOption("udemy_url") != null) {
                    val url = it.getOption("udemy_url")?.asString!!
                    if (!url.matches(Regex("https?://(www\\.)?udemy\\.com/user/.+"))) {
                        it.replyEmbed({
                            title = "Uh oh.."
                            description =
                                "`$url` does not seem to be a valid udemy url. Make sure to follow the instructions or ping a specialist."
                            color = EMBED_COLOR
                        }).queue()
                    }
                    datastore.findOne(Filters.eq("udemy", url)) ?: run {
                        it.replyEmbed({
                            title = "That member does not have a profile"
                            description = "We couldn't find a profile linked with $url"
                            color = EMBED_COLOR
                        }).queue()
                        return@onCommand
                    }
                }else {
                    it.replyEmbed({
                        title = "You must provide a search query"
                        description = "You must provide a member or udemy url to search by"
                        color = EMBED_COLOR
                    }).queue()
                    return@onCommand
                }

                it.replyEmbed({
                    title = "Profile Lookup"
                    color = EMBED_COLOR

                    val member = it.guild!!.getMemberById(profile.id)!!
                    field("Discord", "${member.user.name} (${member.asMention})", inline = false)

                    val udemyService = UdemyService()
                    val udemyProfile = udemyService.lookupProfile(profile.udemyUrl)
                    if(udemyProfile != null) {
                        var udemyProfileField = "[${udemyProfile.username}](${udemyProfile.url}) (${udemyProfile.id}) \nCourses:\n"
                        udemyProfile.ownedCourses.take(5).forEach { course ->
                            udemyProfileField += "\u2022 [${course.title}](${course.url})\n"
                        }
                        if(udemyProfile.ownedCourses.size > 5) {
                            udemyProfileField += "\u2022 & more..."
                        }
                        field("Udemy", udemyProfileField, inline = false)
                    }else {
                        field("Udemy", "Unable to find", inline = false)
                    }

                    field("Reputation", "${profile.reputation.size}", inline = false)
                }, ephemeral = true).queue()
            }
        }.queue()
    }
}