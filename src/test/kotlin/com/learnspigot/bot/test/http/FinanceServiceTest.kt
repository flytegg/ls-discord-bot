package com.learnspigot.bot.test.http

import com.learnspigot.bot.http.FinanceService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 *
 * @project learnspigot-bot
 * @author Nucker
 * @date 02/11/2022
 */
internal class FinanceServiceTest {

    private val service = FinanceService()

    @Test
    fun getPriceOfStock() {
        assertNotNull(service.getPriceOfStock("WIX"))
    }
}