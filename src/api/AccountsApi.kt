package com.github.jflc.api

import com.github.jflc.api.model.AccountsTransferRequest
import com.github.jflc.api.util.accountsExceptionHandler
import com.github.jflc.api.util.toAccountDetailsResponse
import com.github.jflc.api.util.toAccountsTransferResponse
import com.github.jflc.api.util.toListAccountsResponse
import com.github.jflc.api.util.toTransactionDto
import com.github.jflc.service.AccountsService
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
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
        try {
            // execute operation
            val result = service.findAll().toListAccountsResponse()

            // send response
            call.respond(HttpStatusCode.OK, result)
        } catch (ex: Exception) {
            accountsExceptionHandler(call, application.environment.log, ex)
        }
    }

    // get specific account details
    get<AccountsApi.Details> {
        // get request parameters
        val id = it.id

        try {
            // execute operation
            val result = service.findById(id)?.toAccountDetailsResponse()

            // send response
            if (result != null) {
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (ex: Exception) {
            accountsExceptionHandler(call, application.environment.log, ex)
        }
    }

    // transfer money between accounts
    post<AccountsApi.Transfer> {
        // get request content
        val transferRequest = call.receive<AccountsTransferRequest>()
        try {
            // execute operation
            val result = service.transfer(transferRequest.toTransactionDto())
                .toAccountsTransferResponse()

            // send response
            call.respond(HttpStatusCode.Created, result)
        } catch (ex: Exception) {
            accountsExceptionHandler(call, application.environment.log, ex)
        }
    }
}
