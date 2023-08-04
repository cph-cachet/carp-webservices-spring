package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [StudyStatusSerializer].
 * The [StudyStatusSerializer] implements the serialization logic for [StudyStatus].
 */
class StudyStatusSerializer(private val validationMessages: MessageBase): JsonSerializer<StudyStatus>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param studyStatus The [studyStatus] object containing the json object parsed.
     * @throws SerializationException If the [StudyStatus] is blank or empty. Also, if the [StudyStatus] contains invalid format.
     * @return The serialization of [StudyStatus] object.
     */
    override fun serialize(studyStatus: StudyStatus?, jsonGenerator: JsonGenerator?, serializers: SerializerProvider?)
    {
        if (studyStatus == null)
        {
            LOGGER.error("The StudyStatus is null.")
            throw SerializationException(validationMessages.get("study.status.serialization.empty"))
        }

        val serialized: String
        try
        {
            serialized = JSON.encodeToString(studyStatus)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The StudyStatus is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.status.serialization.error", ex.message.toString()))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}