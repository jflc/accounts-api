package com.github.jflc.db.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.util.UUID

/**
 * Accounts Table.
 */
object Accounts : UUIDTable() {
    val name: Column<String> = varchar("name", 50)
    val balance: Column<BigDecimal> = decimal("balance", 15, 2)
        .check("check_positive_balance") {it greaterEq BigDecimal.ZERO }
}

/**
 * Account Entity.
 */
open class AccountEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    var name    by Accounts.name
    var balance by Accounts.balance
}

/**
 * Accounts Repository.
 */
object AccountsRepository : UUIDEntityClass<AccountEntity>(Accounts, AccountEntity::class.java) {

    /**
     * Add amount to balance column.
     *
     * @param id account id
     * @param value update value expression
     * @return number of updated rows
     */
    fun updateBalance(id: UUID, value: (SqlExpressionBuilder.()-> Expression<BigDecimal>)):Int = Accounts.update({ Accounts.id eq id }) {
        with(SqlExpressionBuilder) {
            it.update(balance, value())
        }
    }

    /**
     * Find account balance by id.
     *
     * @param id account id
     * @return balance if exists, null if not
     */
    fun findBalanceById(id: UUID) = Accounts.slice(Accounts.balance)
        .select { Accounts.id eq id }
        .limit(1)
        .map { it[Accounts.balance] }
        .singleOrNull()

}