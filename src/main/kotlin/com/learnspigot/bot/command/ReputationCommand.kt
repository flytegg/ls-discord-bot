package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.LearnSpigotBot.Companion.editEmbed
import com.learnspigot.bot.LearnSpigotBot.Companion.findOne
import com.learnspigot.bot.LearnSpigotBot.Companion.findUserProfile
import com.learnspigot.bot.entity.ReputationPoint
import com.learnspigot.bot.entity.UserProfile
import com.learnspigot.bot.manager.LeaderboardManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.events.onCommandAutocomplete
import dev.minn.jda.ktx.events.onContext
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.interactions.commands.upsertCommand
import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.primary
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.Datastore
import dev.morphia.query.experimental.filters.Filters
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.*
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

class ReputationCommand(private val guild: Guild, private val bot: JDA, private val datastore: Datastore, private val leaderboardManager: LeaderboardManager) {

    private val medals: Array<String> = arrayOf(":first_place:", ":second_place:", ":third_place:")
    private val repPageSize = 10

    fun repCommand() {
        guild.upsertCommand("rep", "Manage your reputation") {
            option<Member>("user", "The user you wish to view the rep of", required = true)
            bot.onCommand("rep") {
                val eventSession = UUID.randomUUID()
                it.deferReply(true).queue()
                val target = it.getOption("user")?.asMember!!
                val profile: UserProfile = datastore.findUserProfile(target.id)
                val lastRepPage = ceil(profile.reputation.size / repPageSize.toDouble())
                val channel = it.channel!!
                if (channel !is TextChannel) return@onCommand
                it.hook.editOriginalEmbeds(generateRepEmbed(profile, 1)).let {editAction ->
                    editAction.setActionRow(
                        primary("$eventSession-rep-prev_0", "Previous page", Emoji.fromUnicode("U+2B05"), disabled = true),
                        primary("$eventSession-rep-next_2", "Next page", Emoji.fromUnicode("U+27A1"),
                            disabled = profile.reputation.size <= repPageSize
                        )
                    )
                    editAction.queue()
                }
                bot.listener<ButtonInteractionEvent> { buttonEvent ->
                    if (!buttonEvent.componentId.contains(eventSession.toString())) return@listener
                    buttonEvent.deferReply(true).queue()
                    buttonEvent.hook.deleteOriginal().queue()
                    val targetPage: Int = buttonEvent.componentId.split("_")[1].toInt()
                    val hasNext = targetPage < lastRepPage
                    val hasPrev = targetPage > 1
                    it.hook.editOriginalEmbeds(generateRepEmbed(profile, targetPage)).let {editAction ->
                        editAction.setActionRow(
                            primary("$eventSession-rep-prev_${targetPage - 1}", "Previous page", Emoji.fromUnicode("U+2B05"), !hasPrev),
                            primary("$eventSession-rep-next_${targetPage + 1}", "Next page", Emoji.fromUnicode("U+27A1"), !hasNext)
                        )
                        editAction.queue()
                    }
                }
            }
        }.queue()
    }

    private fun generateRepEmbed(profile: UserProfile, inputPage: Int = 1): MessageEmbed {
        var endRepIndex = inputPage*repPageSize
        val startRepIndex = repPageSize*(inputPage-1)
        if(profile.reputation.size < endRepIndex) {
            endRepIndex = profile.reputation.size
        }
        return Embed {
            title = "Reputation"
            description = "${guild.getMemberById(profile.id)!!.user.asMention} has ${profile.reputation.size} reputation points"
            description += "\n\nRep ${startRepIndex+1}-$endRepIndex:"
            profile.reputation.sortedWith { o1, o2 ->
                o2.timestamp() compareTo o1.timestamp()
            }.subList(startRepIndex, endRepIndex).forEach { rep ->
                description += "\n\u2022 "
                if (rep.postId != null) {
                    description += "In <#${rep.postId}>, "
                }

                if (rep.fromMemberId != null) {
                    description += "from <@${rep.fromMemberId}> "
                }

                description += "at <t:${rep.epochTimestamp.milliseconds.inWholeSeconds}:f>"
            }
            footer("Page: $inputPage/${ceil(profile.reputation.size / repPageSize.toDouble()).toInt()}")
            color = LearnSpigotBot.EMBED_COLOR
        }
    }

    fun repLeaderboardCommand() {
        guild.upsertCommand("repleaderboard", "The leaderboard for top reputation (Top 10)") {
            restrict(guild = true)
            option<Boolean>("monthly", "Weather the leaderboard should be monthly or not")
            bot.onCommand("repleaderboard") { command ->
                command.deferReply().queue()
                val monthly = command.getOption("monthly")?.asBoolean ?: false
                val topUsers = datastore.find(UserProfile::class.java)
                    .filter { it.reputation.size >= 1 }
                    .map {
                        if(!monthly) return@map it
                        return@map UserProfile(
                            it.id,
                            it.udemyUrl,
                            it.reputation.filter { rep ->
                                YearMonth.now()
                                    .atDay(1)
                                    .atStartOfDay()
                                    .toInstant(ZoneOffset.UTC)
                                    .isBefore(rep.timestamp())
                            }.toMutableList(),
                            it.messageHistory
                        )
                    }
                    .sortedWith { o1, o2 ->
                        o1.reputation.size compareTo o2.reputation.size
                    }
                    .reversed()
                    .take(10)
                command.editEmbed({
                    title = if(monthly) "Monthly Leaderboard" else "All-Time Leaderboard"
                    description = ""
                    topUsers.forEachIndexed {i, profile ->
                        val username = guild.getMemberById(profile.id)?.asMention ?: "*User not found* (`${profile.id}`)"
                        val medal: String = if((i + 1) <= medals.size) {
                            medals[i]
                        }else {
                            ""
                        }
                        description += "\n${i + 1}. $username - ${profile.reputation.size} $medal"
                    }
                }).queue()
            }
        }.queue()
    }

    fun repAdminCommand() {
        guild.upsertCommand("managerep", "Mange the reputation of users") {
            subcommand("add", "Add points to user") {
                restrict(guild = true, DefaultMemberPermissions.DISABLED)
                option<Member>("user", "The user you want to add rep to", required = true)
                option<Member>("from_user", "The user who gave the rep", required = false)
                option<TextChannel>("channel", "The channel the rep came from", required = false)
                bot.onCommand("managerep") {
                    if(!(it.subcommandName != null && it.subcommandName == "add")) return@onCommand
                    it.deferReply().queue()
                    val target = it.getOption("user")?.asMember!!
                    val profile: UserProfile = datastore.findUserProfile(target.id)
                    val rep = ReputationPoint(System.currentTimeMillis(), it.getOption("from_user")?.asMember?.id, it.getOption("channel")?.asChannel?.id)
                    profile.addHelpRep(rep, leaderboardManager, it.guild!!)
                    datastore.save(profile)
                    it.editEmbed({
                        title = "Reputation added"
                        description = "You added 1 reputation point to ${target.asMention}"
                        field("Time", "<t:${rep.epochTimestamp.milliseconds.inWholeSeconds}:f>", false)
                        if(rep.fromMemberId != null) {
                            field("From", "<@${rep.fromMemberId}>", false)
                        }
                        if(rep.postId != null) {
                            field("In", "<#${rep.postId}>", false)
                        }
                    }).queue()
                }
            }

            subcommand("remove", "Remove reputation point from user") {
                option<Member>("user", "The user you want to remove rep from", required = true)
                option<String>("timestamp", "The timestamp of the rep, used to specifically identify it", required = false, autocomplete = true)
                bot.onCommandAutocomplete("managerep") { autoCompleteEvent ->
                    if(autoCompleteEvent.focusedOption.name == "timestamp") {
                        val target = autoCompleteEvent.getOption("user")?.asString!!
                        val profile: UserProfile = datastore.findOne(Filters.eq("id", target)) ?: return@onCommandAutocomplete
                        val choices = profile.reputation.sortedWith { o1, o2 ->
                            o1.timestamp() compareTo o2.timestamp()
                        }.take(5).map { it.epochTimestamp.toString() }
                        autoCompleteEvent.replyChoiceStrings(choices).queue()
                    }
                }

                bot.onCommand("managerep") {
                    if(!(it.subcommandName != null && it.subcommandName == "remove")) return@onCommand
                    it.deferReply().queue()
                    val target = it.getOption("user")?.asMember!!
                    val profile: UserProfile = datastore.findOne(Filters.eq("id", target.id)) ?: run {
                        it.editEmbed({
                            title = "Hang on"
                            description = "This user does not have a profile. Make sure they're verified"
                        }).queue()
                        return@onCommand
                    }

                    if(it.getOption("timestamp") != null) {
                        val timestamp = it.getOption("timestamp")!!.asString.toLong()
                        val toRemoveReps =
                            profile.reputation.filter { rep -> rep.epochTimestamp == timestamp }
                        if(toRemoveReps.isEmpty()) {
                            it.editEmbed({
                                title = "Hmm"
                                description = "I was unable to find a reputation point with the timestamp of <t:${timestamp.milliseconds.inWholeSeconds}:f>."
                            }).queue()
                            return@onCommand
                        }

                        toRemoveReps.forEach { rep ->
                            profile.reputation.remove(rep)
                        }
                        datastore.save(profile)
                        it.editEmbed({
                            description = "Successfully removed ${toRemoveReps.size} point${if(toRemoveReps.isNotEmpty()) "s" else ""} from ${target.asMention} \n" +
                                    "With timestamp <t:${timestamp.milliseconds.inWholeSeconds}:f>"
                        }).queue()
                    }else {
                        profile.reputation.removeLast()
                        datastore.save(profile)
                        it.editEmbed({
                            description = "Successfully removed the most recent reputation point"
                        }).queue()
                    }
                }
            }
        }.queue()
    }

    fun addRepContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Add reputation").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.DISABLED
            }
        ).queue()
        bot.onContext<User>("Add reputation") {
            it.replyModal(Modal("add-rep-${it.target.id}", "Add reputation") {
                short("amount", "Amount (default 1)", false)
                short("channel", "From channel (id)", false)
                short("user", "From user (id)", false)
            }).queue()
        }
        bot.listener<ModalInteractionEvent> {
            if(it.modalId.startsWith("add-rep-")) {
                it.deferReply(true).queue()
                val profile: UserProfile = datastore.findUserProfile(it.modalId.split("-")[2])

                val channel: TextChannel? = if((it.getValue("channel")?.asString ?: "") == "") null else it.guild?.getTextChannelById(it.getValue("channel")!!.asString)
                val user: Member? = if((it.getValue("member")?.asString ?: "") == "") null else it.guild?.getMemberById(it.getValue("member")!!.asString)
                val rep = ReputationPoint(System.currentTimeMillis(), channel?.id, user?.id)
                profile.addHelpRep(rep, leaderboardManager, it.guild!!)
                datastore.save(profile)

                it.editEmbed({
                    title = "Reputation added"
                    description = "You added 1 reputation point to <@${profile.id}>"
                    field("Time", "<t:${rep.epochTimestamp.milliseconds.inWholeSeconds}:f>", false)
                    if(rep.fromMemberId != null) {
                        field("From", "<@${rep.fromMemberId}>", false)
                    }
                    if(rep.postId != null) {
                        field("In", "<#${rep.postId}>", false)
                    }
                }).queue()
            }
        }
    }

    fun removeRepContext() {
        guild.upsertCommand(
            Commands.context(Command.Type.USER, "Remove reputation").also {
                it.isGuildOnly = true
                it.defaultPermissions = DefaultMemberPermissions.DISABLED
            }
        ).queue()
        bot.onContext<User>("Remove reputation") {
            it.deferReply().queue()
            val profile: UserProfile = datastore.findUserProfile(it.target.id)
            profile.reputation.removeLast()
            datastore.save(profile)
            it.editEmbed({
                description = "Successfully removed the most recent reputation point"
            }).queue()
        }
    }

    fun addLeaderboardCommand() {
        guild.upsertCommand("addleaderboard", "Add an updating reputation leaderboard") {
            restrict(guild = true, DefaultMemberPermissions.DISABLED)
            option<Boolean>("monthly", "Weather the leaderboard should be monthly or not")
            bot.onCommand("addleaderboard") {
                it.deferReply(true).queue()
                leaderboardManager.createMessage(it.messageChannel, it.getOption("monthly")?.asBoolean ?: false)
                it.editEmbed({
                    description = "Created leaderboard"
                }).queue()
            }
        }.queue()
    }

    fun addLookupCommand() {
        guild.upsertCommand("addlookup", "Add an lookup message to a channel") {
            restrict(guild = true, DefaultMemberPermissions.DISABLED)
            bot.onCommand("addlookup") {
                it.deferReply(true).queue()
                leaderboardManager.sendLookupMessage(it.messageChannel)
                it.editEmbed({
                    description = "Created lookup message"
                }).queue()
            }
        }.queue()
    }
}