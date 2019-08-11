package com.github.jflc.api.model

import java.math.BigDecimal
import java.util.UUID

/**
 * Transfer request content data.
 */
data class AccountsTransferRequest(
    val requestId: UUID,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val amount: BigDecimal
)