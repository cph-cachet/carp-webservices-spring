package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [StudyDetailsDeserializer].
 * [StudyDetailsDeserializer] implements the deserialization logic for [StudyDetails].
 */
class StudyDetailsDeserializer(private val validationMessages: MessageBase) : JsonDeserializer<StudyDetails>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [StudyDetails] is blank or empty.
     * Also, if the [StudyDetails] contains invalid format.
     * @return The deserialized [StudyDetails] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        context: DeserializationContext?,
    ): StudyDetails {
        val studyDetails: String
        try {
            studyDetails = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studyDetails)) {
                LOGGER.error("The StudyDetails cannot be blank or empty.")
                throw SerializationException(validationMessages.get("study.details.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDetails request contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.details.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: StudyDetails
        try {
            parsed = JSON.decodeFromString(studyDetails)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDetails serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.details.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
