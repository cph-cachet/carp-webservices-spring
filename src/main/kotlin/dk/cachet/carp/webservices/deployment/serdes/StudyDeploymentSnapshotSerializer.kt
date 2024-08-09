package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [StudyDeploymentSnapshotSerializer].
 * The [StudyDeploymentSnapshotSerializer] implements the serialization logic for [StudyDeploymentSnapshot].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyDeploymentSnapshotSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<StudyDeploymentSnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param studyDeploymentSnapshot The [studyDeploymentSnapshot] object containing the json object parsed.
     * @param jsonGenerator The [jsonGenerator] to write JSON content generated from the study deployment snapshot.
     * @param serializers The [serializers] to serialize the parsed object from the study deployment snapshot.
     * @throws SerializationException If the [StudyDeploymentSnapshot] is blank or empty.
     * Also, if the [StudyDeploymentSnapshot] contains invalid format.
     * @return The serialization of deployment service request object.
     */
    override fun serialize(
        studyDeploymentSnapshot: StudyDeploymentSnapshot?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (studyDeploymentSnapshot == null) {
            LOGGER.error("The core StudyDeploymentSnapshot is null.")
            throw SerializationException(validationMessages.get("deployment.study_deployment.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(studyDeploymentSnapshot)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDeploymentSnapshot is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.study_deployment.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
