package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [StudyProtocolSnapshotDeserializer].
 * The [StudyProtocolSnapshotDeserializer] implements the serialization logic for [StudyProtocolSnapshot].
 */
class StudyProtocolSnapshotDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<StudyProtocolSnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [StudyProtocolSnapshot] is blank or empty.
     * Also, if the [StudyProtocolSnapshot] contains invalid format.
     * @return The deserialized [StudyProtocolSnapshot] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): StudyProtocolSnapshot {
        val studyProtocolSnapshot: String
        try {
            studyProtocolSnapshot = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studyProtocolSnapshot)) {
                LOGGER.error("The core StudyProtocolSnapshot cannot be blank or empty.")
                throw SerializationException(validationMessages.get("protocol.snapshot.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core StudyProtocolSnapshot contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.snapshot.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: StudyProtocolSnapshot
        try {
            parsed = JSON.decodeFromString(studyProtocolSnapshot)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyProtocolSnapshot serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.snapshot.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
