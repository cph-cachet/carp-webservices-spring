package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WSInputDataTypes.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [StudyServiceRequestSerializer].
 * The [StudyServiceRequestSerializer] implements the serialization logic for [StudyServiceRequest].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyServiceRequestSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<StudyServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param studyServiceRequest The [studyServiceRequest] object containing the json object parsed.
     * @throws SerializationException If the [StudyServiceRequest] is blank or empty.
     * Also, if the [StudyServiceRequest] contains invalid format.
     * @return The serialization of [StudyServiceRequest] object.
     */
    override fun serialize(
        studyServiceRequest: StudyServiceRequest<*>?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (studyServiceRequest == null) {
            LOGGER.error("The StudyServiceRequest is null.")
            throw SerializationException(validationMessages.get("study.service.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(StudyServiceRequest.Serializer, studyServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The core study request is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.service.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
