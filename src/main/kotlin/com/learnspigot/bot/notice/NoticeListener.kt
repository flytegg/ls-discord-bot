package com.learnspigot.bot.notice

import com.learnspigot.bot.notice.types.AiNotice
import com.learnspigot.bot.notice.types.McUtilsNotice
import com.learnspigot.bot.notice.types.NoticeType
import com.learnspigot.bot.notice.types.PastebinNotice
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import java.util.concurrent.TimeUnit

class NoticeListener : ListenerAdapter() {

    private val notifyMap: Map<String, NoticeType> = mapOf(
        "ai" to AiNotice(),
        "mcutils" to McUtilsNotice(),
        "pastebin" to PastebinNotice(),
    )

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        var componentId = event.componentId
        if (!componentId.startsWith("notice-")) return
        componentId = componentId.substring("notice-".length)

        val userId = componentId.substring(componentId.indexOf("-") + 1)
        val hook = event.hook

        if (componentId.startsWith("ai")) {
            notifyUser(hook, userId, "ai")
        }
        else if (componentId.startsWith("mcutils")) {
            notifyUser(hook, userId, "mcutils")
        }
        else if (componentId.startsWith("pastebin")) {
            notifyUser(hook, userId, "pastebin")
        }
    }

    private fun notifyUser(hook: InteractionHook, userId: String?, notificationType: String) {
        if (userId == null) return

        hook.sendMessage("<@${userId}>").queue { message ->
            run {
                hook.deleteMessageById(message.idLong).queueAfter(500, TimeUnit.MILLISECONDS)
            }
        }

        val notifyType = notifyMap[notificationType] ?: return

        hook.sendMessageEmbeds(notifyType.notifyEmbed(userId).build()).queue()
    }
}