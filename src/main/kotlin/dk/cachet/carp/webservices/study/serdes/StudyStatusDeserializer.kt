package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyStatusDeserializer(private val validationMessages: MessageBase) : JsonDeserializer<StudyStatus>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun deserialize(
        jsonParser: JsonParser?,
        context: DeserializationContext?,
    ): StudyStatus {
        val studyStatus: String
        try {
            studyStatus = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studyStatus)) {
                LOGGER.error("The StudyStatus cannot be blank or empty.")
                throw SerializationException(validationMessages.get("study.status.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The StudyStatus contains bad format. Exception: $ex")
            throw SerializationException(validationMessages.get("study.status.deserialization.bad_format"))
        }

        val parsed: StudyStatus
        try {
            parsed = JSON.decodeFromString(studyStatus)
        } catch (ex: Exception) {
            LOGGER.error("The StudyStatus is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("study.status.deserialization.error"))
        }

        return parsed
    }
}
