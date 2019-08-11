package com.github.jflc.service.exception

import java.util.UUID

/**
 * Exception raised when some operation tries to set an account balance with negative values.
 */
class InsufficientAccountBalanceException(val accountId: UUID): Exception("Insufficient account balance '$accountId'")