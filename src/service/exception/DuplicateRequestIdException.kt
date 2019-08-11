package com.github.jflc.service.exception

import java.util.UUID

/**
 * Exception raised when a transfer is requested with an existing request id.
 */
class DuplicateRequestIdException(val requestId: UUID) : Exception("Duplicate request id '$requestId'")