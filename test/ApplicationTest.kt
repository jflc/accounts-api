package com.github.jflc

import com.github.jflc.service.AccountsService
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

@KtorExperimentalLocationsAPI
internal class ApplicationTest {

    private val accountsService = mockk<AccountsService>()

    @Test
    fun testHealthCheck() {
        withTestApplication({ accountsModule(accountsService) }) {
            handleRequest(HttpMethod.Get, "/healthcheck").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("UP", response.content)
            }
        }
    }
}
