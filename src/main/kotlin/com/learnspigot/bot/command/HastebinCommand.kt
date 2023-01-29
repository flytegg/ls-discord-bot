package com.learnspigot.bot.command

import com.learnspigot.bot.LearnSpigotBot
import com.learnspigot.bot.http.HastebinService
import dev.minn.jda.ktx.events.onContext
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands

class HastebinCommand(private val guild: Guild, private val bot: JDA) {
  val hastebin = HastebinService()
  fun uploadCodeBlocks() {

    guild.upsertCommand(
      Commands.context(Command.Type.MESSAGE, "Upload Code Blocks").also {
        it.isGuildOnly = true
      }
    ).queue()

    bot.onContext<Message>("Upload Code Blocks") {
      val message = it.target

      val codeBlockSplit = message.contentRaw.split("```")
      val regex = Regex("(^[^ \\n]*\\n)?([\\s\\S]*+)")

      println(codeBlockSplit.size)
      val codeBlockContents = codeBlockSplit.mapIndexedNotNull { index, matchResult ->
        if (index % 2 == 0) return@mapIndexedNotNull null
        val content = regex.find(matchResult)?.groups?.get(2)?.value
        if (content.isNullOrBlank()) return@mapIndexedNotNull null else content
      }
      if (codeBlockContents.isEmpty()) {
        it.reply("No full code blocks found in message.").setEphemeral(true).queue()
        return@onContext
      }

      val uploadedHastebinUrls = codeBlockContents.mapNotNull { hastebin.uploadHastebin("https://paste.learnspigot.com", it) }

      it.replyEmbeds(Embed {
        description = "Reuploaded code blocks to [our Hastebin server](<https://paste.learnspigot.com/>)!"
        color = LearnSpigotBot.EMBED_COLOR
        field {
          name = "Re-uploaded Pastes"
          value =
            uploadedHastebinUrls.joinToString("\n") { data -> "\u2022 [${data.getUrl()}](<${data.getUrl()}>)" }
        }
      }).queue()

    }
  }
}