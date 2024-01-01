package com.learnspigot.bot.stats

import com.learnspigot.bot.Bot
import com.learnspigot.bot.Environment
import com.learnspigot.bot.Server
import com.learnspigot.bot.reputation.LeaderboardMessage
import gg.flyte.neptune.annotation.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color
import java.util.*

class WrappedCommand {

  private val guild = Bot.jda.getGuildById(Environment.get("GUILD_ID"))!!
  private val currentYear = 2023
  private val helpForum: ForumChannel = Bot.jda.getForumChannelById(Server.helpChannel.id)!!

  private val uniqueUsers = mutableMapOf<String, Int>()
  private var totalMessages = 0
  private val channelsMessageCount = mutableMapOf<String, Int>()
  private val emojisUsageCount = mutableMapOf<String, Int>()
  private val wordsUsageCount = mutableMapOf<String, Int>()
  private var studentsHelped = 0
  private val highestContributor: LeaderboardMessage.ReputationWrapper by lazy {
    LeaderboardMessage.top10(Bot().profileRegistry(), false).firstOrNull()!!
  }

  private lateinit var mostReactedMessage: net.dv8tion.jda.api.entities.Message

  private val blacklist = mutableListOf(
    "the", "to", "it", "i", "a", "and", "is", "in", "that", "you",
    "was", "for", "on", "are", "with", "as", "at", "be", "this",
    "have", "from", "or", "an", "but", "not", "by", "we", "can",
    "if", "they", "he", "she", "will", "all", "no", "there", "do",
    "just", "has", "so", "what", "about", "up", "out", "up", "one",
    "down", "into", "some", "your", "how", "like", "when", "his",
    "her", "their", "would", "who", "which", "time", "than", "them",
    ":", "of", "count", "last", "since"
  )

  @Command(
    name = "wrapped",
    description = "get this year's discord wrapped",
    permissions = [Permission.MANAGE_ROLES]
  )
  fun onWrappedCommand(
    event: SlashCommandInteractionEvent,
  ) {
    event.reply("Fetching the stats, please hold a moment!").setEphemeral(true).queue()
    performMessageIteration()
    performHelpIteration()
    event.channel.sendMessageEmbeds(buildRecapMessage().build()).queue()
  }

  private fun performMessageIteration() {
    guild.textChannels.forEach { textChannel ->
      textChannel.iterableHistory.forEach { message ->
        val messageYear = message.timeCreated.year
        if (messageYear == currentYear) {
          totalMessages++
          uniqueUsers.merge(message.author.id, 1, Int::plus)

          val reactions = message.reactions.size
          if (!::mostReactedMessage.isInitialized ||
            reactions > mostReactedMessage.reactions.size
          ) {
            mostReactedMessage = message
          }

          channelsMessageCount.merge(textChannel.id, 1, Int::plus)

          val emojis = guild.emojis
          emojis.forEach { emoji ->
            val emojiId = emoji.id
            if (message.contentRaw.contains(emojiId)) {
              emojisUsageCount.merge(emojiId, 1, Int::plus)
            }
          }

          val words = message.contentRaw.split("\\s+".toRegex())
          words.forEach { word ->
            wordsUsageCount.merge(word, 1, Int::plus)
          }
        }
      }
    }
  }

  private fun performHelpIteration() {
    studentsHelped = helpForum.threadChannels.count { it.isArchived && it.timeCreated.year == currentYear }
  }

  private fun buildRecapMessage(): EmbedBuilder {
    val topUsers = uniqueUsers.entries.sortedByDescending { it.value }.take(5)
    val topChannels = channelsMessageCount.entries.sortedByDescending { it.value }.take(3)
    val topEmojis = emojisUsageCount.entries.sortedByDescending { it.value }.take(5)
    val topWords = wordsUsageCount.entries
      .filter { it.key.lowercase(Locale.getDefault()) !in blacklist }
      .sortedByDescending { it.value }
      .take(5)


    val codeBlock = buildString {
      append("```\n")

      append("Total Messages: $totalMessages\n\n")

      append("Top 5 Most Active Users:\n")
      topUsers.forEach { entry ->
        append("${Bot.jda.getUserById(entry.key)!!.asMention}: ${entry.value} messages\n")
      }

      append("\nTop 3 Most Active Channels:\n")
      topChannels.forEach { entry ->
        val channel = guild.getTextChannelById(entry.key)
        append("${channel!!.asMention}: ${entry.value} messages\n")
      }

      append("\nTop 5 Most Used Emojis:\n")
      topEmojis.forEach { entry ->
        val emojiId = entry.key
        val emoji = guild.getEmojiById(emojiId)
        append("${emoji?.asMention ?: "<:$emojiId>"}: ${entry.value} uses\n")
      }

      append("\nTop 5 Most Used Words:\n")
      topWords.forEach { entry -> append("${entry.key}: ${entry.value} uses\n") }

      append("\nHelp Post Statistics:\n")
      append("Help posts successfully closed: $studentsHelped\n")
      append(
        "Highest Contributor: ${Bot.jda.getUserById(highestContributor.id)!!.asMention} with ${highestContributor.reputation.size} reps!\n"
      )

      append("```\n")
    }

    return EmbedBuilder()
      .setColor(Color.BLUE)
      .setTitle("Discord Wrapped $currentYear")
      .setDescription("Here's a recap of this year's Discord activity:")
      .addField("LearnSpigot Discord Wrapped $currentYear", codeBlock, false)
  }
}
