package com.github.jflc.service

import com.github.jflc.db.dbInit
import com.github.jflc.service.exception.AccountNotFoundException
import com.github.jflc.service.exception.DuplicateRequestIdException
import com.github.jflc.service.exception.InsufficientAccountBalanceException
import com.github.jflc.service.model.TransactionDto
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull


internal enum class TestAccount(id: String) {
    JOAO("aaee2b13-8a5e-4aed-a30b-5d8535c8ab20"),
    NIKOLAY("1ce455f7-f30c-4f55-81e2-7df2e8f88c7d"),
    LEMMY("fb789eb9-a5a9-4ebe-a808-a9cd59b19772"),
    NON_EXISTENT("53a53ae9-b826-4847-8a50-7223ba0a5ad6");

    val id: UUID = UUID.fromString(id)
}

private val testDb = dbInit {
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    driver = "org.h2.Driver"
}

internal class AccountsServiceTest {

    @Test
    fun testFindAllWithResults() = runBlocking {
        // given
        val service = AccountsService(testDb)

        // when
        val result = service.findAll()

        // then
        assertEquals(3, result.size)
    }

    @Test
    fun testFindAllWithoutResults() = runBlocking {
        // given
        val emptyDb = dbInit {
            url = "jdbc:h2:mem:empty;DB_CLOSE_DELAY=-1"
            driver = "org.h2.Driver"
            initData = false
        }
        val service = AccountsService(emptyDb)

        // when
        val result =  service.findAll()

        // then
        assertEquals(0, result.size)
    }

    @Test
    fun testFindByExistingId() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val account = TestAccount.JOAO

        // when
        val result = service.findById(account.id)

        // then
        assertNotNull(result)
        assertEquals(account.id, result.id)
    }

    @Test
    fun testFindByNonExistentId() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val account = TestAccount.NON_EXISTENT

        // when
        val result = service.findById(account.id)

        // then
        assertNull(result)
    }

    @Test
    fun testValidTransfer() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val request = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = TestAccount.NIKOLAY.id,
            toAccountId = TestAccount.JOAO.id,
            amount = BigDecimal(100000.00)
        )
        val beforeFromAccount = service.findById(request.fromAccountId)!!
        val beforeToAccount = service.findById(request.toAccountId)!!

        // when
        val result = service.transfer(request)

        // then
        assertNotNull(result)
        assertNotNull(result.requestId)
        assertNotNull(result.createdAt)
        assertEquals(request.fromAccountId, result.fromAccountId)
        assertEquals(request.toAccountId, result.toAccountId)
        assertEquals(request.amount, result.amount)
        val afterFromAccount = service.findById(request.fromAccountId)!!
        val afterToAccount = service.findById(request.toAccountId)!!
        assertEquals(beforeFromAccount.balance - request.amount, afterFromAccount.balance)
        assertEquals(beforeToAccount.balance + request.amount, afterToAccount.balance)
    }

    @Test
    fun testTransferFromNonExistentAccount() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val request = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = TestAccount.NON_EXISTENT.id,
            toAccountId = TestAccount.LEMMY.id,
            amount = BigDecimal(1234567.89)
        )
        val beforeToAccount = service.findById(request.toAccountId)!!

        // when
        val result = assertFailsWith(AccountNotFoundException::class) {
            service.transfer(request)
        }

        // then
        assertEquals(request.fromAccountId, result.accountId)
        val afterToAccount = service.findById(request.toAccountId)!!
        assertEquals(beforeToAccount.balance, afterToAccount.balance)
    }

    @Test
    fun testTransferToNonExistentAccount() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val request = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = TestAccount.LEMMY.id,
            toAccountId = TestAccount.NON_EXISTENT.id,
            amount = BigDecimal(1234567.89)
        )
        val beforeFromAccount = service.findById(request.fromAccountId)!!

        // when
        val result = assertFailsWith(AccountNotFoundException::class) {
            service.transfer(request)
        }

        // then
        assertEquals(request.toAccountId, result.accountId)
        val afterFromAccount = service.findById(request.fromAccountId)!!
        assertEquals(beforeFromAccount.balance, afterFromAccount.balance)
    }

    @Test
    fun testTransferWithoutSufficientBalance() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val request = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = TestAccount.JOAO.id,
            toAccountId = TestAccount.LEMMY.id,
            amount = BigDecimal(1234567.89)
        )
        val beforeFromAccount = service.findById(request.fromAccountId)!!
        val beforeToAccount = service.findById(request.toAccountId)!!

        // when
        val result = assertFailsWith(InsufficientAccountBalanceException::class) {
            service.transfer(request)
        }

        // then
        assertEquals(request.fromAccountId, result.accountId)
        val afterFromAccount = service.findById(request.fromAccountId)!!
        val afterToAccount = service.findById(request.toAccountId)!!
        assertEquals(beforeFromAccount.balance, afterFromAccount.balance)
        assertEquals(beforeToAccount.balance, afterToAccount.balance)
    }

    @Test
    fun testTransferWithDuplicatedRequestId() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val request = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = TestAccount.JOAO.id,
            toAccountId = TestAccount.LEMMY.id,
            amount = BigDecimal.ONE
        )
        val beforeFromAccount = service.findById(request.fromAccountId)!!
        val beforeToAccount = service.findById(request.toAccountId)!!

        // when
        service.transfer(request)
        val result = assertFailsWith(DuplicateRequestIdException::class) {
            service.transfer(request)
        }

        // then
        assertEquals(request.requestId, result.requestId)
        val afterFromAccount = service.findById(request.fromAccountId)!!
        val afterToAccount = service.findById(request.toAccountId)!!
        assertEquals(beforeFromAccount.balance - BigDecimal.ONE, afterFromAccount.balance)
        assertEquals(beforeToAccount.balance + BigDecimal.ONE, afterToAccount.balance)
    }

    @Test
    fun testTransferConcurrency() = runBlocking {
        // given
        val service = AccountsService(testDb)
        val request = TransactionDto(
            requestId = UUID.randomUUID(),
            fromAccountId = TestAccount.NIKOLAY.id,
            toAccountId = TestAccount.JOAO.id,
            amount = BigDecimal(10000.00)
        )
        val numRequests = 25
        val beforeFromAccount = service.findById(request.fromAccountId)!!
        val beforeToAccount = service.findById(request.toAccountId)!!

        // when
        coroutineScope {
            repeat(numRequests) {
                launch {
                    service.transfer(
                        TransactionDto(
                            requestId = UUID.randomUUID(),
                            fromAccountId = request.fromAccountId,
                            toAccountId = request.toAccountId,
                            amount = request.amount
                        )
                    )
                }
            }
        }

        // then
        val afterFromAccount = service.findById(request.fromAccountId)!!
        val afterToAccount = service.findById(request.toAccountId)!!
        val expectedDifference = request.amount * BigDecimal(numRequests)
        assertEquals(beforeFromAccount.balance - expectedDifference, afterFromAccount.balance)
        assertEquals(beforeToAccount.balance + expectedDifference, afterToAccount.balance)
    }
}