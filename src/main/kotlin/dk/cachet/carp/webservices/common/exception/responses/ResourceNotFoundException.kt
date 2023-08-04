package dk.cachet.carp.webservices.common.exception.responses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Class [ResourceNotFoundException].
 * The [ResourceNotFoundException] exception is thrown when a resource cannot be found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String?): RuntimeException(message)