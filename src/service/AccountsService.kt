package com.github.jflc.service

import com.github.jflc.db.model.Accounts
import com.github.jflc.db.model.AccountsRepository
import com.github.jflc.db.model.TransactionsRepository
import com.github.jflc.service.exception.AccountNotFoundException
import com.github.jflc.service.exception.DuplicateRequestIdException
import com.github.jflc.service.exception.InsufficientAccountBalanceException
import com.github.jflc.service.model.AccountDto
import com.github.jflc.service.model.TransactionDto
import com.github.jflc.service.util.toAccountDto
import com.github.jflc.service.util.toTransactionDto
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.joda.time.DateTime
import java.util.UUID

/**
 * Service responsible for all accounts operations.
 */
class AccountsService(private val db: Database) {

    /**
     * Find all accounts.
     *
     * @return list of accounts
     */
    suspend fun findAll(): List<AccountDto> = newSuspendedTransaction(Dispatchers.IO, db) {
        AccountsRepository.all().map { it.toAccountDto() }
    }

    /**
     * Find account by id.
     *
     * @param id account id
     * @return account if exists, null if not
     */
    suspend fun findById(id: UUID): AccountDto? = newSuspendedTransaction(Dispatchers.IO, db) {
        AccountsRepository.findById(id)?.toAccountDto()
    }

    /**
     * Transfer money from one account to another.
     *
     * @param request transfer request data
     * @return transaction details
     * @throws AccountNotFoundException some account doesn't exists
     * @throws InsufficientAccountBalanceException from account doesn't have sufficient balance to perform the transfer
     * @throws DuplicateRequestIdException request id already exists
     */
    suspend fun transfer(request: TransactionDto): TransactionDto = newSuspendedTransaction(Dispatchers.IO, db) {
        // validate request data
        validateTransferRequest(request)

        // update accounts balance
        updateAccountsBalance(request)

        // create transfer transaction
        val result = createTransferTransaction(request)

        result.toTransactionDto()
    }

    /**
     * Validate transfer request data.
     *
     * @param request transfer request data
     * @throws AccountNotFoundException some account doesn't exists
     * @throws InsufficientAccountBalanceException from account doesn't have sufficient balance to perform the transfer
     * @throws DuplicateRequestIdException request id already exists
     */
    private fun validateTransferRequest(request: TransactionDto) {
        // check if request id already exists
        if (TransactionsRepository.existsByRequestId(request.requestId)) throw DuplicateRequestIdException(request.requestId)
        // check if from account exists
        val fromAccountBalance = AccountsRepository.findBalanceById(request.fromAccountId) ?: throw AccountNotFoundException(request.fromAccountId)
        // check if to account exists
        AccountsRepository.findBalanceById(request.toAccountId) ?: throw AccountNotFoundException(request.toAccountId)
        // check if from account balance is sufficient
        if (fromAccountBalance < request.amount) throw InsufficientAccountBalanceException(request.fromAccountId)
    }

    /**
     * Update from and to accounts balance.
     *
     * @param request transfer request data
     */
    private fun updateAccountsBalance(request: TransactionDto) {
        AccountsRepository.updateBalance(request.fromAccountId) {Accounts.balance - request.amount}
        AccountsRepository.updateBalance(request.toAccountId) {Accounts.balance + request.amount}
    }

    /**
     * Create transfer transaction.
     *
     * @param request transfer request data
     * @return transaction dto
     */
    private fun createTransferTransaction(request: TransactionDto) = TransactionsRepository.new {
        requestId = request.requestId
        fromAccountId = request.fromAccountId
        toAccountId = request.toAccountId
        amount = request.amount
        createdAt = DateTime.now()
    }

}