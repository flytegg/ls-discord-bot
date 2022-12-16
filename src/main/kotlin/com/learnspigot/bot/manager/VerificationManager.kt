package com.learnspigot.bot.manager

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.http.UdemyService
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import dev.morphia.query.filters.Filters
import net.dv8tion.jda.api.entities.Member

class VerificationManager(private val datastore: Datastore) {

    enum class VerificationResponse {
        NOT_OWNED, INVALID_URL, ALREADY_VERIFIED, DUPLICATE_PROFILE, SUCCESS
    }

    fun verifyUser(member: Member, url: String, force: Boolean = false): VerificationResponse {
        val role = member.guild.getRoleById(System.getenv("STUDENT_ROLE_ID"))!!
        if(force) {
            welcomeUser(member, url)
            return VerificationResponse.SUCCESS
        }
        if (member.roles.contains(role)) {
            return VerificationResponse.ALREADY_VERIFIED
        }
        if (!url.matches(Regex("https?://(www\\.)?udemy\\.com/user/.+"))) {
            return VerificationResponse.INVALID_URL
        }

        if (datastore.find(UserProfile::class.java).filter(Filters.eq("udemy", url)).count() >= 1) {
            // URL already exists
            return VerificationResponse.ALREADY_VERIFIED
        }

        // Initial validation complete
        val udemyService = UdemyService()
        return if (udemyService.studentOwnsCourse(url)) {
            // Student owns course
            welcomeUser(member, url)
            VerificationResponse.SUCCESS
        } else {
            // Does not own course
            VerificationResponse.NOT_OWNED
        }
    }

    private fun welcomeUser(member: Member, url: String) {
        val guild = member.guild
        val role = guild.getRoleById(System.getenv("STUDENT_ROLE_ID"))!!
        val profile: UserProfile = datastore.findUserProfile(member.id)
        profile.udemyUrl = url
        datastore.save(profile)
        guild.addRoleToMember(member, role).queue()
        guild.getTextChannelById(System.getenv("GENERAL_CHANNEL_ID")!!)!!.sendMessageEmbeds(Embed {
            title = "Welcome"
            description = "Please welcome ${member.asMention} as a student! :heart:"
            color = LearnSpigotBot.EMBED_COLOR
        }).queue()
    }

    fun unverifyUser(member: Member) {
        datastore.find(UserProfile::class.java).filter(Filters.eq("id", member.id)).let {
            if(it.count() == 0L) throw NullPointerException("Unable to find profile")
            it.delete()
        }
        member.guild.removeRoleFromMember(member, member.guild.getRoleById(System.getenv("STUDENT_ROLE_ID"))!!).queue()
    }
}