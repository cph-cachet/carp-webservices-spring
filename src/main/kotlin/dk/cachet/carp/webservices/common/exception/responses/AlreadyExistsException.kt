package dk.cachet.carp.webservices.common.exception.responses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Class [AlreadyExistsException].
 * The [AlreadyExistsException] is thrown when user is trying to save an entity that already exists .
 */
@ResponseStatus(HttpStatus.CONFLICT)
class AlreadyExistsException(message: String?) : RuntimeException(message)
