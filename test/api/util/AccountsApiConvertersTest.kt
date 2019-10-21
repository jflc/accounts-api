package api.util

import com.github.jflc.api.model.AccountsTransferRequest
import com.github.jflc.api.util.toAccountDetailsResponse
import com.github.jflc.api.util.toAccountsTransferResponse
import com.github.jflc.api.util.toListAccountsResponse
import com.github.jflc.api.util.toTransactionDto
import com.github.jflc.service.model.AccountDto
import com.github.jflc.service.model.TransactionDto
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AccountsApiConvertersTest {

    @Test
    fun testListAccountDtoToListAccountsResponse() {
        // given
        val dto = listOf(
            AccountDto(UUID.randomUUID(), "Dummy 0", BigDecimal.ZERO),
            AccountDto(UUID.randomUUID(), "Dummy 1", BigDecimal.ONE)
        )

        // when
        val result = dto.toListAccountsResponse()

        // then
        assertEquals(2, result.results.size)
        assertEquals(dto[0].id, result.results[0].id)
        assertEquals(dto[1].id, result.results[1].id)
    }

    @Test
    fun testAccountDtoToAccountDetailsResponse() {
        // given
        val dto = AccountDto(
            id = UUID.fromString("35b20b27-b049-4d9d-95f0-e6b244708f36"),
            name = "dummy account",
            balance = BigDecimal.TEN
        )

        // when
        val result = dto.toAccountDetailsResponse()

        // then
        assertEquals("35b20b27-b049-4d9d-95f0-e6b244708f36", result.id.toString())
        assertEquals("dummy account", result.name)
        assertEquals("10.00", result.balance.setScale(2).toPlainString())

    }

    @Test
    fun testAccountsTransferRequestToTransactionDto() {
        // given
        val request = AccountsTransferRequest(
            requestId = UUID.fromString("8dad0fca-57a7-4f6c-af24-b6fd7ed763da"),
            amount = BigDecimal.valueOf(12.34).setScale(2),
            toAccountId = UUID.fromString("3557fde0-593d-4c69-abaf-f84967689e8a"),
            fromAccountId = UUID.fromString("e8367e47-f68f-4597-ae59-bc3fc3bb18c5")
        )

        // when
        val result = request.toTransactionDto()

        // then
        assertEquals("8dad0fca-57a7-4f6c-af24-b6fd7ed763da", result.requestId.toString())
        assertEquals("12.34", result.amount.toPlainString())
        assertEquals("3557fde0-593d-4c69-abaf-f84967689e8a", result.toAccountId.toString())
        assertEquals("e8367e47-f68f-4597-ae59-bc3fc3bb18c5", result.fromAccountId.toString())
    }

    @Test
    fun testTransactionDtoToAccountsTransferResponse() {
        // given
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dto = TransactionDto(
            requestId = UUID.fromString("6301c657-1ff8-4e37-af9d-abee235b8aaa"),
            amount = BigDecimal.ZERO.setScale(1),
            createdAt = dateFormat.parse("2019-10-21"),
            fromAccountId = UUID.fromString("022d7eac-16c3-4850-b227-35ceac0dd8d4"),
            toAccountId = UUID.fromString("d4f7f0da-4cde-42b5-8f16-30710cab515f")
        )

        // when
        val result = dto.toAccountsTransferResponse()

        // then
        assertEquals("6301c657-1ff8-4e37-af9d-abee235b8aaa", result.requestId.toString())
        assertEquals("0.0", result.amount.toPlainString())
        assertEquals("2019-10-21", dateFormat.format(result.at))
    }
}