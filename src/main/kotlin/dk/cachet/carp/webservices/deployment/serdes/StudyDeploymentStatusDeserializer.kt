package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [StudyDeploymentStatusDeserializer].
 * [StudyDeploymentStatusDeserializer] implements the deserialization logic for [StudyDeploymentStatus].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyDeploymentStatusDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<StudyDeploymentStatus>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [StudyDeploymentStatus] is blank or empty.
     * Also, if the [StudyDeploymentStatus] contains invalid format.
     * @return The deserialised study deployment status object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): StudyDeploymentStatus {
        val studyDeploymentStatus: String
        try {
            studyDeploymentStatus = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studyDeploymentStatus)) {
                LOGGER.error("The core StudyDeploymentStatus cannot be blank or empty.")
                throw SerializationException(validationMessages.get("deployment.status.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDeploymentStatus contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.status.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: StudyDeploymentStatus
        try {
            parsed = WS_JSON.decodeFromString(studyDeploymentStatus)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDeploymentStatus serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.status.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
