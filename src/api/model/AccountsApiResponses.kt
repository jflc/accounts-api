package com.github.jflc.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

/**
 * List accounts API response.
 */
data class ListAccountsResponse(val results: List<Item>) {
    /**
     * List accounts API response item data.
     */
    data class Item(
        val id: UUID
    )
}

/**
 * Get specific account details API Response.
 */
data class AccountDetailsResponse(
    val id: UUID,
    val name: String,
    val balance: BigDecimal
)

/**
 * Transfer response data.
 */
data class AccountsTransferResponse(
    val requestId: UUID,
    val amount: BigDecimal,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'X'")
    val at: Date
)

/**
 * Error response message.
 */
data class ErrorResponse(
    val code: Int,
    val message: String,
    val affectedValues: List<Any>
)