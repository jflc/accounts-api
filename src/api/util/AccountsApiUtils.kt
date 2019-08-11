package com.github.jflc.api.util

import com.github.jflc.api.model.AccountDetailsResponse
import com.github.jflc.api.model.AccountsTransferRequest
import com.github.jflc.api.model.AccountsTransferResponse
import com.github.jflc.api.model.ErrorResponse
import com.github.jflc.api.model.ListAccountsResponse
import com.github.jflc.service.exception.AccountNotFoundException
import com.github.jflc.service.exception.DuplicateRequestIdException
import com.github.jflc.service.exception.InsufficientAccountBalanceException
import com.github.jflc.service.model.AccountDto
import com.github.jflc.service.model.TransactionDto
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.slf4j.Logger

/**
 * Handles Accounts API exceptions.
 *
 * @param call api call
 * @param log application logger
 * @param ex exception to handle
 */
suspend fun accountsExceptionHandler(call: ApplicationCall, log: Logger, ex: Exception) {

    when(ex) {
        is AccountNotFoundException -> {
            log.info(ex.message)
            val result = ErrorResponse(1, "Account doesn't exists", listOf(ex.accountId))
            call.respond(HttpStatusCode.BadRequest, result)
        }
        is DuplicateRequestIdException -> {
            log.warn(ex.message)
            val result = ErrorResponse(2, "Request id already processed", listOf(ex.requestId))
            call.respond(HttpStatusCode.Conflict, result)
        }
        is InsufficientAccountBalanceException -> {
            log.info(ex.message)
            val result = ErrorResponse(3, "Insufficient balance to perform transfer", listOf(ex.accountId))
            call.respond(HttpStatusCode.BadRequest, result)
        }
        else -> {
            log.error("Unexpected exception", ex)
            val result = ErrorResponse(Int.MAX_VALUE, "Unexpected internal error", emptyList())
            call.respond(HttpStatusCode.InternalServerError, result)
        }
    }
}

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