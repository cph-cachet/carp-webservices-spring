package dk.cachet.carp.webservices.common.exception.responses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Class [ConflictException].
 * The [ConflictException] with a [HttpStatus.CONFLICT] is thrown if the request is conflicting with the state of the
 * server.
 */
@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String?) : RuntimeException(message)
