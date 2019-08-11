package com.github.jflc.service.model

import java.math.BigDecimal
import java.util.UUID

/**
 * Account Data Transfer Object.
 */
data class AccountDto(
    val id: UUID,
    val name: String,
    val balance: BigDecimal
)