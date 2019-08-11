package com.github.jflc.service.exception

import java.util.UUID

/**
 * Exception raised when some operation can't find the respective account.
 */
class AccountNotFoundException(val accountId: UUID): Exception("Account not found '$accountId'")