package com.learnspigot.bot.test.http

import com.learnspigot.bot.http.UdemyService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UdemyServiceTest {

    private val service = UdemyService()

    @Test
    fun verifyStudent() {
        assertTrue(service.studentOwnsCourse("https://www.udemy.com/user/daniel-devenish/"))
        assertFalse(service.studentOwnsCourse("https://www.udemy.com/user/ls-burner-2/"))
    }
}