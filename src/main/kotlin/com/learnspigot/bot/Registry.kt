package com.learnspigot.bot

import com.learnspigot.bot.counting.CountingRegistry
import com.learnspigot.bot.profile.ProfileRegistry

object Registry {
    val PROFILE = ProfileRegistry()
    val COUNTING = CountingRegistry()
}