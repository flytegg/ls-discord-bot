package com.learnspigot.bot.entity

import com.learnspigot.bot.LearnSpigotBot.Companion.EMBED_COLOR
import com.learnspigot.bot.manager.LeaderboardManager
import dev.minn.jda.ktx.messages.Embed
import dev.morphia.annotations.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

@Entity("users")
@Indexes(
    Index(fields = arrayOf(Field("udemy")))
)
data class UserProfile(
    @Id val id: String,
    @Property("udemy") var udemyUrl: String = "https://www.udemy.com/null",
    val reputation: MutableList<ReputationPoint> = mutableListOf(),
    val messageHistory: MutableList<SerializedMessage> = mutableListOf(),
    var reputationPing : Boolean = false
) {
    fun addRep(channelSource: MessageChannel?, memberSource: Member?, leaderboardManager: LeaderboardManager, guild: Guild) {
        addRep(ReputationPoint(System.currentTimeMillis(), memberSource?.id, channelSource?.id), leaderboardManager, guild)
    }

    fun addRep(point: ReputationPoint, leaderboardManager: LeaderboardManager, guild: Guild) {
        reputation.add(point)
        acknowledgeRep(point, leaderboardManager, guild)
    }

    fun addRep(leaderboardManager: LeaderboardManager, guild: Guild, reason: String, count: Int) {
        for (rep in count.downTo(1)) {
            reputation.add(ReputationPoint(System.currentTimeMillis(), null, null))
        }
        leaderboardManager.updateLeaderboardMessages()
        val channel = guild.getTextChannelById(System.getenv("SUPPORT_CHANNEL_ID"))!!
        if (reputationPing)
            channel.sendMessage("<@${id}>").queue{ it.delete().queue{} }
        channel.sendMessageEmbeds(Embed {
            title = "Reputation Added!"
            description = "<@$id> has received $count rep point${if (count > 1) "s" else ""} from making a $reason"
            color = EMBED_COLOR
        }).queue()
    }
    private fun acknowledgeRep(point: ReputationPoint, leaderboardManager: LeaderboardManager, guild: Guild) {
        leaderboardManager.updateLeaderboardMessages()
        val channel = guild.getTextChannelById(System.getenv("SUPPORT_CHANNEL_ID"))!!
        if(point.postId != null) {
            if (reputationPing)
                channel.sendMessage("<@${id}>").queue{ it.delete().queue{} }
            channel.sendMessageEmbeds(Embed {
                title = "Reputation Added!"
                description = "<@$id> has received a rep point from <#${point.postId}>"
                color = EMBED_COLOR
            }).queue()
        }
    }
}
