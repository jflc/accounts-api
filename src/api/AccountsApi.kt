package com.github.jflc.api

import com.github.jflc.api.model.AccountsTransferRequest
import com.github.jflc.api.model.ErrorResponse
import com.github.jflc.api.util.toAccountDetailsResponse
import com.github.jflc.api.util.toAccountsTransferResponse
import com.github.jflc.api.util.toListAccountsResponse
import com.github.jflc.api.util.toTransactionDto
import com.github.jflc.service.AccountsService
import com.github.jflc.service.exception.AccountNotFoundException
import com.github.jflc.service.exception.DuplicateRequestIdException
import com.github.jflc.service.exception.InsufficientAccountBalanceException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import org.slf4j.Logger
import java.util.UUID

/**
 * Accounts API locations.
 */
@KtorExperimentalLocationsAPI
@Location("/accounts")
class AccountsApi {

    /**
     * List all accounts.
     */
    @Location("/")
    class List

    /**
     * Get details of a single account.
     *
     * @param id account id
     */
    @Location("/{id}")
    data class Details(val id: UUID)

    /**
     * Transfer money between accounts.
     */
    @Location("/transfer")
    class Transfer
}

/**
 * Accounts API operations.
 *
 * @param service service to operate account actions
 */
@KtorExperimentalLocationsAPI
fun Route.accounts(service: AccountsService) {

    // list all accounts
    get<AccountsApi.List> {
        // execute operation
        val result = service.findAll().toListAccountsResponse()

        // send response
        call.respond(HttpStatusCode.OK, result)
    }

    // get specific account details
    get<AccountsApi.Details> {
        // get request parameters
        val id = it.id

        // execute operation
        val result = service.findById(id)?.toAccountDetailsResponse()

        // send response
        if (result != null) {
            call.respond(HttpStatusCode.OK, result)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    // transfer money between accounts
    post<AccountsApi.Transfer> {
        // get request content
        val transferRequest = call.receive<AccountsTransferRequest>()

        // execute operation
        val result = service.transfer(transferRequest.toTransactionDto())
            .toAccountsTransferResponse()

        // send response
        call.respond(HttpStatusCode.Created, result)
    }
}

/**
 * Handles Accounts API exceptions.
 *
 * @param call api call
 * @param log application logger
 * @param ex exception to handle
 */
suspend fun accountsExceptionHandler(call: ApplicationCall, log: Logger, ex: Throwable) {

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
