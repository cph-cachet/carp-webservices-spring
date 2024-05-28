package dk.cachet.carp.webservices.common.exception.serialization

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Serialization Exception Class [SerializationException].
 * The [SerializationException] throws [HttpStatus.BAD_REQUEST] exception if the serializer cannot serialize the value e.g., bad format or empty objects.
 *
 * @param message The error [message] provided.
 * @throws [HttpStatus.BAD_REQUEST].
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class SerializationException(message: String?) : RuntimeException(message)
