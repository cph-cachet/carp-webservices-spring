package dk.cachet.carp.webservices.common.serialisers

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.KSerializer

/**
 * Serializes and deserializes requests to and from the application service.
 */
abstract class ApplicationRequestSerializer<TRequest : ApplicationServiceRequest<*, *>> {
    companion object {
        val json = WS_JSON
    }

    /**
     * Deserialize the request from the specified [content].
     */
    fun <TRequest> deserializeRequest(
        serializer: KSerializer<TRequest>,
        content: String,
    ): TRequest {
        return json.decodeFromString(serializer, content)
    }

    /**
     * Serialize the specified [request] to a string.
     */
    abstract fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any?
}
