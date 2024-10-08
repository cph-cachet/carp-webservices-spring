package dk.cachet.carp.webservices.protocol.serdes

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.serialisers.ApplicationRequestSerializer
import kotlinx.serialization.serializer

/**
 * Serializes and deserializes [ProtocolServiceRequest] to and from the application service.
 * This is used to deserialize requests from the application service and serialize responses to the application service.
 */
class ProtocolRequestSerializer : ApplicationRequestSerializer<ProtocolServiceRequest<*>>() {
    @Suppress("UNCHECKED_CAST")
    override fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any? {
        return when (request) {
            is ProtocolServiceRequest.Add,
            is ProtocolServiceRequest.AddVersion,
            -> content
            is ProtocolServiceRequest.UpdateParticipantDataConfiguration,
            is ProtocolServiceRequest.GetBy,
            ->
                json.encodeToString(serializer<StudyProtocolSnapshot>(), content as StudyProtocolSnapshot)
            is ProtocolServiceRequest.GetAllForOwner ->
                json.encodeToString(serializer<List<StudyProtocolSnapshot>>(), content as List<StudyProtocolSnapshot>)
            is ProtocolServiceRequest.GetVersionHistoryFor ->
                json.encodeToString(serializer<List<ProtocolVersion>>(), content as List<ProtocolVersion>)
            else -> content
        }
    }
}

/**
 * Serializes and deserializes [ProtocolFactoryServiceRequest] to and from the application service.
 * This is used to deserialize requests to the application service and serialize responses from the application service.
 */
class ProtocolFactoryRequestSerializer : ApplicationRequestSerializer<ProtocolFactoryServiceRequest<*>>() {
    override fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any? {
        return when (request) {
            is ProtocolFactoryServiceRequest.CreateCustomProtocol ->
                json.encodeToString(serializer<StudyProtocolSnapshot>(), content as StudyProtocolSnapshot)
            else -> content
        }
    }
}
