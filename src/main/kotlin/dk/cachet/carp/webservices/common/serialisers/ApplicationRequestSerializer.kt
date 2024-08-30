package dk.cachet.carp.webservices.common.serialisers

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.KSerializer

/**
 * Serializes and deserializes requests to and from the application service.
 * This is used to serialize requests to the application service and deserialize responses from the application service.
 *
 * @param TRequest The type of request which can be serialized and deserialized.
 */
abstract class ApplicationRequestSerializer<TRequest : ApplicationServiceRequest<*, *>> {
    companion object {
        val json = WS_JSON
    }

    /**
     * Deserialize the request from the specified [content].
     *
     * @param serializer The serializer to use to deserialize the request.
     * @param content The content to deserialize from.
     * @return The deserialized request object.
     */
    fun <TRequest> deserializeRequest(
        serializer: KSerializer<TRequest>,
        content: String,
    ): TRequest {
        return json.decodeFromString(serializer, content)
    }

    /**
     * Serialize the specified [request] to a string.
     *
     * @param request The request body to serialize.
     * @param content The content to serialize.
     * @return The serialized request string.
     */
    abstract fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any?
}
