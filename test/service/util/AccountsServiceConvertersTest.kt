package com.github.jflc.service.util

import com.github.jflc.db.model.AccountEntity
import com.github.jflc.db.model.TransactionEntity
import io.mockk.every
import io.mockk.mockk
import org.joda.time.DateTime
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AccountsServiceConvertersTest {

    @Test
    fun testAccountEntityToAccountDto() {
        // given
        val entity = mockk<AccountEntity>()
        every { entity.id.value } returns UUID.fromString("aafca1e8-610d-443f-8878-41f86f078862")
        every { entity.name } returns  "dummy account"
        every { entity.balance } returns  BigDecimal.ONE

        // when
        val result = entity.toAccountDto()

        // then
        assertEquals("aafca1e8-610d-443f-8878-41f86f078862", result.id.toString())
        assertEquals("dummy account", result.name)
        assertEquals(BigDecimal.ONE, result.balance)
    }

    @Test
    fun testTransactionEntityToTransactionDto() {
        // given
        val now = Instant.now().toEpochMilli()
        val entity = mockk<TransactionEntity>()
        every { entity.requestId  } returns UUID.fromString("728a0dc6-5502-49e6-8f5e-d584e5537926")
        every { entity.fromAccountId } returns UUID.fromString("7d1efe7e-e209-4924-a681-4cd33d05159a")
        every { entity.toAccountId } returns UUID.fromString("d5f4125b-ddcd-4f32-974f-0a5ec9eab279")
        every { entity.amount } returns BigDecimal.valueOf(1904, 0)
        every { entity.createdAt } returns DateTime(now)

        // when
        val result = entity.toTransactionDto()

        // then
        assertEquals("728a0dc6-5502-49e6-8f5e-d584e5537926", result.requestId.toString())
        assertEquals("7d1efe7e-e209-4924-a681-4cd33d05159a", result.fromAccountId.toString())
        assertEquals("d5f4125b-ddcd-4f32-974f-0a5ec9eab279", result.toAccountId.toString())
        assertEquals("1904", result.amount.toPlainString())
        assertEquals(now, result.createdAt!!.time)
    }
}