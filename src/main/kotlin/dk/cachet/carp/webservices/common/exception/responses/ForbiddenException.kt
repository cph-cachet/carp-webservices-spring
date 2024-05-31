package dk.cachet.carp.webservices.common.exception.responses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Class [ForbiddenException].
 * The [ForbiddenException] is thrown when the user is not authorized/permitted to perform the request.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException(message: String?) : RuntimeException(message)
