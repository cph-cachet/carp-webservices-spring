package dk.cachet.carp.webservices.common.exception.responses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Class [UnauthorizedException].
 * The [UnauthorizedException] with a [HttpStatus.UNAUTHORIZED] is thrown if the user is not authorized.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException(message: String?) : RuntimeException(message)
