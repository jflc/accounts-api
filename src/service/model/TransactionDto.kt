package com.github.jflc.service.model

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

/**
 * Transaction Data Transfer Object.
 */
data class TransactionDto (
    val requestId: UUID,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val amount: BigDecimal,
    val createdAt: Date? = null
)