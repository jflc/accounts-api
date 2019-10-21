package com.github.jflc.api.util

import com.github.jflc.api.model.AccountDetailsResponse
import com.github.jflc.api.model.AccountsTransferRequest
import com.github.jflc.api.model.AccountsTransferResponse
import com.github.jflc.api.model.ListAccountsResponse
import com.github.jflc.service.model.AccountDto
import com.github.jflc.service.model.TransactionDto

/**
 * Convert list of AccountDto to ListAccountsResponse.
 */
fun List<AccountDto>.toListAccountsResponse() = ListAccountsResponse(
    results = this.map { ListAccountsResponse.Item(it.id) }
)

/**
 * Convert AccountDto to AccountDetailsResponse.
 */
fun AccountDto.toAccountDetailsResponse() = AccountDetailsResponse(
    id = this.id,
    name = this.name,
    balance = this.balance
)

/**
 * Convert AccountsTransferRequest to TransactionDto.
 */
fun AccountsTransferRequest.toTransactionDto() = TransactionDto(
    requestId = this.requestId,
    fromAccountId = this.fromAccountId,
    toAccountId = this.toAccountId,
    amount = this.amount
)

/**
 * Convert TransactionDto to AccountsTransferResponse.
 */
fun TransactionDto.toAccountsTransferResponse() = AccountsTransferResponse(
    requestId = this.requestId,
    amount = this.amount,
    at = this.createdAt!!
)