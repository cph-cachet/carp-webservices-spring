package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [StudyDeploymentStatusSerializer].
 * [StudyDeploymentStatusSerializer] implements the serialization logic for [StudyDeploymentStatus].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class StudyDeploymentStatusSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<StudyDeploymentStatus>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param studyDeploymentStatus The [studyDeploymentStatus] object containing the json object parsed.
     * @param jsonGenerator The [jsonGenerator] to write JSON content generated from the study deployment status.
     * @param serializers The [serializers] to serialize the parsed object from the study deployment status.
     * @throws SerializationException If the [StudyDeploymentStatus] is blank or empty.
     * Also, if the [StudyDeploymentStatus] contains invalid format.
     * @return The serialization of deployment service request object.
     */
    override fun serialize(
        studyDeploymentStatus: StudyDeploymentStatus?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (studyDeploymentStatus == null) {
            LOGGER.error("The core StudyDeploymentStatus is null.")
            throw SerializationException(validationMessages.get("deployment.status.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(studyDeploymentStatus)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyDeploymentStatus is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.status.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
