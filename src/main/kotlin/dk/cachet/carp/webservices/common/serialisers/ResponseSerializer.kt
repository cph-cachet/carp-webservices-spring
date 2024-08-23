package dk.cachet.carp.webservices.common.serialisers

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.KSerializer

abstract class ResponseSerializer<TRequest : ApplicationServiceRequest<*, *>> {
    companion object {
        val json = WS_JSON
    }

    fun <TRequest> deserializeRequest(
        serializer: KSerializer<TRequest>,
        content: String,
    ): TRequest {
        return json.decodeFromString(serializer, content)
    }

    abstract fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any?
}
