package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [StudyDetailsSerializer].
 * The [StudyDetailsSerializer] implements the serialization logic for [StudyDetails].
 */
class StudyDetailsSerializer(private val validationMessages: MessageBase) : JsonSerializer<StudyDetails>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param studyDetails The [studyDetails] object containing the json object parsed.
     * @throws SerializationException If the [StudyDetails] is blank or empty. Also, if the [StudyDetails] contains invalid format.
     * @return The serialization of [StudyDetails] object.
     */
    override fun serialize(
        studyDetails: StudyDetails?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (studyDetails == null) {
            LOGGER.error("The StudyDetails is null.")
            throw SerializationException(validationMessages.get("study.details.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(studyDetails)
        } catch (ex: Exception) {
            LOGGER.error("The StudyDetails is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("study.details.serialization.error"))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
