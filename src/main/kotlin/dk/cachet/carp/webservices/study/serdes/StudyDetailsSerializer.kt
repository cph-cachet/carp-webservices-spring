package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyDetailsSerializer(private val validationMessages: MessageBase) : JsonSerializer<StudyDetails>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

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
            serialized = WS_JSON.encodeToString(studyDetails)
        } catch (ex: Exception) {
            LOGGER.error("The StudyDetails is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("study.details.serialization.error"))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
