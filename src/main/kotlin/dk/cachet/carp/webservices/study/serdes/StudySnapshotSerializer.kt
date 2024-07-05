package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [StudySnapshotSerializer].
 * The [StudySnapshotSerializer] implements the serialization logic for [StudySnapshot].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudySnapshotSerializer(private val validationMessages: MessageBase) : JsonSerializer<StudySnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param studySnapshot The [studySnapshot] object containing the json object parsed.
     * @throws SerializationException If the [StudySnapshot] is blank or empty.
     * Also, if the [StudySnapshot] contains invalid format.
     * @return The serialization of [StudySnapshot] object.
     */
    override fun serialize(
        studySnapshot: StudySnapshot?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (studySnapshot == null) {
            LOGGER.error("The StudySnapshot is null.")
            throw SerializationException(validationMessages.get("study.snapshot.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(studySnapshot)
        } catch (ex: Exception) {
            LOGGER.error("The StudySnapshot is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.snapshot.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
