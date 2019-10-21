package com.github.jflc.db

import com.github.jflc.db.model.Accounts
import com.github.jflc.db.model.AccountsRepository
import com.github.jflc.db.model.Transactions
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.UUID

/**
 * Database configuration properties.
 */
class DatabaseConfiguration {
    var url: String = "jdbc:h2:mem:default"
    var driver: String = "org.h2.Driver"
    var initSchema: Boolean = true
    var initData: Boolean = true
}

/**
 * Initialize configuration.
 *
 * @param configure apply database configurations
 */
fun dbInit(configure: DatabaseConfiguration.() -> Unit): Database {
    val configuration = DatabaseConfiguration().apply(configure)
    val db = Database.connect(configuration.url, configuration.driver)

    if (configuration.initSchema) {
        dbInitSchema(db)
    }
    if (configuration.initData) {
        dbInitData(db)
    }

    return db
}

/**
 * Initialize database schema.
 */
private fun dbInitSchema(db: Database) {
    transaction(db) {
        SchemaUtils.create(Accounts, Transactions)
    }
}

/**
 * Initialize database data.
 */
private fun dbInitData(db: Database) {
    transaction(db) {
        // Create Joao Cardoso account
        AccountsRepository.new(UUID.fromString("aaee2b13-8a5e-4aed-a30b-5d8535c8ab20")) {
            name = "Joao Cardoso"
            balance = BigDecimal.valueOf(0.50)
        }
        // Create Satoshi Nakamoto
        AccountsRepository.new(UUID.fromString("1ce455f7-f30c-4f55-81e2-7df2e8f88c7d")) {
            name = "Satoshi Nakamoto"
            balance = BigDecimal.valueOf(15048509238.35)
        }
        // Create Lemmy Kilmister account
        AccountsRepository.new(UUID.fromString("fb789eb9-a5a9-4ebe-a808-a9cd59b19772")) {
            name = "Lemmy Kilmister"
            balance = BigDecimal.valueOf(100.20)
        }
    }
}