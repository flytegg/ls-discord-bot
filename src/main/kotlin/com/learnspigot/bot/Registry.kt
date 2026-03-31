package com.learnspigot.bot

import com.learnspigot.bot.counting.CountingRegistry
import com.learnspigot.bot.help.search.HelpPostRegistry
import com.learnspigot.bot.intellijkey.IJUltimateKeyRegistry
import com.learnspigot.bot.knowledgebase.KnowledgebasePostRegistry
import com.learnspigot.bot.profile.ProfileRegistry
import com.learnspigot.bot.starboard.StarboardRegistry
import com.learnspigot.bot.videos.udemy.UdemyRegistry
import com.learnspigot.bot.videos.youtube.YouTubeRegistry
import com.learnspigot.bot.workshop.WorkShopPostRegistry

object Registry {
    val PROFILES = ProfileRegistry()
    val COUNTING = CountingRegistry()
    val UDEMY = UdemyRegistry()
    val YOUTUBE = YouTubeRegistry()
    val STARBOARD = StarboardRegistry()
    val IJ_ULTIMATE_KEYS = IJUltimateKeyRegistry()
    val KNOWLEDGEBASE = KnowledgebasePostRegistry()
    val HELP = HelpPostRegistry()
    val WORKSHOP = WorkShopPostRegistry()
}