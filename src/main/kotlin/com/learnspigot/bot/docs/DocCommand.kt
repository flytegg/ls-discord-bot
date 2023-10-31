package com.learnspigot.bot.docs

import com.google.gson.Gson
import com.learnspigot.bot.Environment
import com.learnspigot.bot.util.embed
import gg.flyte.neptune.annotation.Command
import gg.flyte.neptune.annotation.Description
import gg.flyte.neptune.annotation.Inject
import gg.flyte.neptune.annotation.Optional
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.math.acosh

class DocCommand {

//    @Inject
//    private lateinit var docRegistry: DocRegistry

//    fun onDocCommand(
//        event: SlashCommandInteractionEvent,
//        @Description("The element you want to search") query: String
//    ) {
//        val result: DocRegistry.QueryResult? = docRegistry.search(query)
//        if (result == null) {
//            event.reply("No element on the Spigot Javadocs was found from your query. You can search classes or specific methods. Some example search terms: 'Player', 'Player#setHealth', 'setHealth'.").setEphemeral(true).queue()
//            return
//        }
//
//        when (result) {
//            is DocRegistry.ClassQueryResult -> {
//                val extendsString = result.classExtends.joinToString(", ")
//                val implementsString = result.classImplements.joinToString(", ")
//
//                val extendsImplements = buildString {
//                    if (extendsString.isNotEmpty()) {
//                        append("extends $extendsString")
//                    }
//                    if (implementsString.isNotEmpty()) {
//                        if (extendsString.isNotEmpty()) {
//                            append(" ")
//                        }
//                        append("implements $implementsString")
//                    }
//                }
//
//                event.replyEmbeds(
//                    embed()
//                        .setDescription(result.classTitle + " " + extendsImplements)
//                        .addField("", result.description, false)
//                        .setFooter("Excerpt from the Spigot Javadocs", "https://i.imgur.com/zMyhPb7.png")
//                        .build()
//                ).addActionRow(
//                    Button.link("https://stephen.gg", "Open Javadoc")
//
//                ).queue()
//            }
//
//            is DocRegistry.MethodQueryResult -> {
//
//            }
//        }
//    }

}