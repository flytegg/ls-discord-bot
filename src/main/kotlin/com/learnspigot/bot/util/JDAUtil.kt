package com.learnspigot.bot.util

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

/** Queues an ephemeral reply of [message] to the event.
 *  Equivalent to `reply(message).setEphemeral(true).queue()`
 */
fun IReplyCallback.replyEphemeral(message: String) = reply(message).setEphemeral(true).queue()