package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [StudyDeploymentSnapshotDeserializer].
 * [StudyDeploymentSnapshotDeserializer] implements the deserialization logic for [StudyDeploymentSnapshot].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyDeploymentSnapshotDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<StudyDeploymentSnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [StudyDeploymentSnapshot] is blank or empty.
     * Also, if the [StudyDeploymentSnapshot] contains invalid format.
     * @return The deserialized study deployment snapshot object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): StudyDeploymentSnapshot {
        val studyDeploymentSnapshot: String
        try {
            studyDeploymentSnapshot = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studyDeploymentSnapshot)) {
                LOGGER.error("The core StudyDeploymentSnapshot cannot be blank or empty.")
                throw SerializationException(validationMessages.get("deployment.study_snapshot.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDeploymentSnapshot contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.study_snapshot.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: StudyDeploymentSnapshot
        try {
            parsed = JSON.decodeFromString(studyDeploymentSnapshot)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDeploymentSnapshot is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.study_snapshot.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
