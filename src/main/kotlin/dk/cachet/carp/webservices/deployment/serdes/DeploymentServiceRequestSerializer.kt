package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [DeploymentServiceRequestSerializer].
 * The [DeploymentServiceRequestSerializer] implements the serialization logic for [DeploymentServiceRequest].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DeploymentServiceRequestSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<DeploymentServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param deploymentServiceRequest The [deploymentServiceRequest] object containing the json object parsed.
     * @param jsonGenerator The [jsonGenerator] to write JSON content generated from the deployment service request.
     * @param serializers The [serializers] to serialize the parsed object from the deployment  service request.
     * @throws SerializationException If the [DeploymentServiceRequest] is blank or empty.
     * Also, if the [DeploymentServiceRequest] contains invalid format.
     * @return The serialization of deployment service request object.
     */
    override fun serialize(
        deploymentServiceRequest: DeploymentServiceRequest<*>?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (deploymentServiceRequest == null) {
            LOGGER.error("The DeploymentServiceRequest value is null.")
            throw SerializationException(validationMessages.get("deployment.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(DeploymentServiceRequest.Serializer, deploymentServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The DeploymentServiceRequest is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
