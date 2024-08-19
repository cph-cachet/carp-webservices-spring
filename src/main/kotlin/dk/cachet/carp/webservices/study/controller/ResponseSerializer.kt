package dk.cachet.carp.webservices.study.controller

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.serializer

@Suppress("UNCHECKED_CAST")
fun <TService : ApplicationService<TService, *>> serializeResponse(
    request: ApplicationServiceRequest<TService, *>,
    content: Any?,
): Any? {
    return when (request) {
        is StudyServiceRequest -> {
            when (content) {
                is StudyStatus ->
                    WS_JSON.encodeToString(serializer<StudyStatus>(), content)
                is StudyDetails ->
                    WS_JSON.encodeToString(serializer<StudyDetails>(), content)
                is List<*> ->
                    WS_JSON.encodeToString(serializer<List<StudyStatus>>(), content as List<StudyStatus>)
                is Boolean ->
                    WS_JSON.encodeToString(serializer<Boolean>(), content)
                else -> content
            }
        }
        is ProtocolServiceRequest -> {
            when (content) {
                is StudyProtocolSnapshot ->
                    WS_JSON.encodeToString(serializer<StudyProtocolSnapshot>(), content)
                is Unit -> content
                is List<*> ->
                    WS_JSON.encodeToString(
                        serializer<List<StudyProtocolSnapshot>>(),
                        content as List<StudyProtocolSnapshot>,
                    )
                is ProtocolVersion ->
                    WS_JSON.encodeToString(serializer<ProtocolVersion>(), content)
                else -> content
            }
        }
        else -> content
    }
}
