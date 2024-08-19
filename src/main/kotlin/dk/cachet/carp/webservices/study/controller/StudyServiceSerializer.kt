package dk.cachet.carp.webservices.study.controller

import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.serializer

@Suppress("UNCHECKED_CAST")
fun serializeRequest(
    request: ApplicationServiceRequest<StudyService, *>,
    content: Any?,
): String {
    return when (content) {
        is StudyStatus ->
            WS_JSON.encodeToString(serializer<StudyStatus>(), content)
        is StudyDetails ->
            WS_JSON.encodeToString(serializer<StudyDetails>(), content)
        is List<*> ->
            WS_JSON.encodeToString(serializer<List<StudyStatus>>(), content as List<StudyStatus>)
        is Boolean ->
            WS_JSON.encodeToString(serializer<Boolean>(), content)
        else -> "Unsupported content type $request."
    }
}
