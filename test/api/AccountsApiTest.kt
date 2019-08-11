package com.github.jflc.api

import com.github.jflc.accountsModule
import com.github.jflc.service.AccountsService
import com.github.jflc.service.exception.AccountNotFoundException
import com.github.jflc.service.exception.DuplicateRequestIdException
import com.github.jflc.service.exception.InsufficientAccountBalanceException
import com.github.jflc.service.model.AccountDto
import com.github.jflc.service.model.TransactionDto
import com.jayway.jsonpath.JsonPath
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val DUMMY_TRANSFER_REQUEST =  """
{
    "requestId": "${UUID.randomUUID()}",
    "fromAccountId": "${UUID.randomUUID()}",
    "toAccountId": "${UUID.randomUUID()}",
    "amount": 1904.00
}
""".trimIndent()

@KtorExperimentalLocationsAPI
internal class AccountsApiTest {

    private val accountsService = mockk<AccountsService>()

    @Test
    fun testListAccountWithResults() {
        // given
        coEvery { accountsService.findAll() } returns listOf(
            AccountDto(
                id = UUID.randomUUID(),
                name = "dummy name 0",
                balance = BigDecimal.ZERO
            ),
            AccountDto(
                id = UUID.randomUUID(),
                name = "dummy name 1",
                balance = BigDecimal.ONE
            )
        )

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Get, "/accounts").apply {
                // then
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(2, JsonPath.read(response.content, "$.results.length()"))
            }
        }
    }

    @Test
    fun testListAccountWithoutResults() {
        // given
        coEvery { accountsService.findAll() } returns emptyList()

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Get, "/accounts").apply {
                // then
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(0, JsonPath.read(response.content, "$.results.length()"))
            }
        }
    }

    @Test
    fun testGetExistingAccount() {
        // given
        val id = UUID.randomUUID()
        coEvery { accountsService.findById(id) } returns AccountDto(
            id = id,
            name = "dummy name",
            balance = BigDecimal.ZERO
        )

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Get, "/accounts/$id").apply {
                // then
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(id.toString(), JsonPath.read(response.content, "$.id"))
            }
        }
    }

    @Test
    fun testGetNonExistingAccount() {
        // given
        val id = UUID.randomUUID()
        coEvery { accountsService.findById(id) } returns null

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Get, "/accounts/$id").apply {
                // then
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testTransferSuccessfully() {
        // given
        val requestBody = DUMMY_TRANSFER_REQUEST
        val serviceResult = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = UUID.randomUUID(),
            toAccountId = UUID.randomUUID(),
            amount = BigDecimal(1904.00),
            createdAt = Date()
        )
        coEvery { accountsService.transfer(any()) } returns serviceResult

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Post, "/accounts/transfer") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(requestBody)
            }.apply {
                // then
                assertEquals(HttpStatusCode.Created, response.status())
                assertEquals(serviceResult.requestId.toString(), JsonPath.read(response.content, "$.requestId"))
            }
        }
    }

    @Test
    fun testTransferWithInvalidAccount() {
        // given
        val requestBody = DUMMY_TRANSFER_REQUEST
        val accountId = UUID.randomUUID()
        coEvery { accountsService.transfer(any()) } throws AccountNotFoundException(accountId)

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Post, "/accounts/transfer") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(requestBody)
            }.apply {
                // then
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals(1, JsonPath.read(response.content, "$.code"))
                assertTrue(JsonPath.read<String>(response.content, "$.message").isNotBlank())
                assertEquals(1, JsonPath.read(response.content, "$.affectedValues.length()"))
                assertEquals(accountId.toString(), JsonPath.read(response.content, "$.affectedValues[0]"))
            }
        }
    }

    @Test
    fun testTransferWithInvalidBalance() {
        // given
        val requestBody = DUMMY_TRANSFER_REQUEST
        val accountId = UUID.randomUUID()
        coEvery { accountsService.transfer(any()) } throws InsufficientAccountBalanceException(accountId)

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Post, "/accounts/transfer") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(requestBody)
            }.apply {
                // then
                assertEquals(HttpStatusCode.BadRequest, response.status())
                assertEquals(3, JsonPath.read(response.content, "$.code"))
                assertTrue(JsonPath.read<String>(response.content, "$.message").isNotBlank())
                assertEquals(1, JsonPath.read(response.content, "$.affectedValues.length()"))
                assertEquals(accountId.toString(), JsonPath.read(response.content, "$.affectedValues[0]"))
            }
        }
    }

    @Test
    fun testTransferWithInvalidRequestId() {
        // given
        val requestBody = DUMMY_TRANSFER_REQUEST
        val requestId = UUID.randomUUID()
        coEvery { accountsService.transfer(any()) } throws DuplicateRequestIdException(requestId)

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Post, "/accounts/transfer") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(requestBody)
            }.apply {
                // then
                assertEquals(HttpStatusCode.Conflict, response.status())
                assertEquals(2, JsonPath.read(response.content, "$.code"))
                assertTrue(JsonPath.read<String>(response.content, "$.message").isNotBlank())
                assertEquals(1, JsonPath.read(response.content, "$.affectedValues.length()"))
                assertEquals(requestId.toString(), JsonPath.read(response.content, "$.affectedValues[0]"))
            }
        }
    }

    @Test
    fun testTransferWithUnexpectedError() {
        // given
        val requestBody = DUMMY_TRANSFER_REQUEST
        coEvery { accountsService.transfer(any()) } throws Exception()

        withTestApplication({ accountsModule(accountsService) }) {
            // when
            handleRequest(HttpMethod.Post, "/accounts/transfer") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(requestBody)
            }.apply {
                // then
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                assertEquals(Int.MAX_VALUE, JsonPath.read(response.content, "$.code"))
                assertTrue(JsonPath.read<String>(response.content, "$.message").isNotBlank())
                assertEquals(0, JsonPath.read(response.content, "$.affectedValues.length()"))
            }
        }
    }
}