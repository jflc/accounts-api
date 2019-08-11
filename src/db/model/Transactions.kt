package com.github.jflc.db.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.math.BigDecimal
import java.util.UUID

/**
 * Transactions Table.
 */
object Transactions : IntIdTable() {
    val requestId: Column<UUID> = uuid("requestId").uniqueIndex("unique_transaction_request_id")
    val fromAccountId: Column<UUID> = uuid("fromAccountId").references(Accounts.id)
    val toAccountId: Column<UUID> = uuid("toAccountId").references(Accounts.id)
    val amount: Column<BigDecimal> = decimal("amount", 15, 2)
    val createdAt: Column<DateTime> = datetime("createdAt")
}

/**
 * Transactions Entity.
 */
open class TransactionEntity(id: EntityID<Int>) : IntEntity(id) {
    var requestId    by Transactions.requestId
    var fromAccountId by Transactions.fromAccountId
    var toAccountId by Transactions.toAccountId
    var amount by Transactions.amount
    var createdAt by Transactions.createdAt
}

/**
 * Transactions Repository.
 */
object TransactionsRepository : IntEntityClass<TransactionEntity>(Transactions, TransactionEntity::class.java) {

    /**
     * Verify if a transaction exists by request id
     *
     * @param requestId transaction request id
     * @return true if exists, false if not
     */
    fun existsByRequestId(requestId: UUID) = Transactions.slice(Transactions.id)
        .select { Transactions.requestId eq requestId }
        .limit(1)
        .any()

}