package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [StudySnapshotDeserializer].
 * The [StudySnapshotDeserializer] implements the deserialization logic for [StudySnapshot].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudySnapshotDeserializer(private val validationMessages: MessageBase) : JsonDeserializer<StudySnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [StudySnapshot] is blank or empty.
     * Also, if the [StudySnapshot] contains invalid format.
     * @return The deserialized [StudySnapshot] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        context: DeserializationContext?,
    ): StudySnapshot {
        val studySnapshot: String
        try {
            studySnapshot = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studySnapshot)) {
                LOGGER.error("The StudySnapshot cannot be blank or empty.")
                throw SerializationException(validationMessages.get("study.snapshot.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The StudySnapshot contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.snapshot.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: StudySnapshot
        try {
            parsed = WS_JSON.decodeFromString(studySnapshot)
        } catch (ex: Exception) {
            LOGGER.error("The core StudySnapshot is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.snapshot.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
