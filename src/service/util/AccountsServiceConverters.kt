package com.github.jflc.service.util

import com.github.jflc.db.model.AccountEntity
import com.github.jflc.db.model.TransactionEntity
import com.github.jflc.service.model.AccountDto
import com.github.jflc.service.model.TransactionDto

/**
 * Convert AccountEntity to AccountDto.
 */
fun AccountEntity.toAccountDto() = AccountDto(
    id = this.id.value,
    name = this.name,
    balance = this.balance
)

/**
 * Convert TransactionEntity to TransactionDto.
 */
fun TransactionEntity.toTransactionDto() = TransactionDto(
    requestId = this.requestId,
    fromAccountId = this.fromAccountId,
    toAccountId = this.toAccountId,
    amount = this.amount,
    createdAt = this.createdAt.toDate()
)