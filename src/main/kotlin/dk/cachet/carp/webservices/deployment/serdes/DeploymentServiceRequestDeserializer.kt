package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [DeploymentServiceRequestDeserializer].
 * The [DeploymentServiceRequestDeserializer] implements the deserialization logic for [DeploymentServiceRequest].
 */
class DeploymentServiceRequestDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<DeploymentServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [DeploymentServiceRequest] is blank or empty.
     * Also, if the [DeploymentServiceRequest] contains invalid format.
     * @return The deserialized deployment service request object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): DeploymentServiceRequest<*> {
        val deploymentServiceRequest: String
        try {
            deploymentServiceRequest = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(deploymentServiceRequest)) {
                LOGGER.error("The DeploymentServiceRequest cannot be blank or empty.")
                throw SerializationException(validationMessages.get("deployment.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The DeploymentServiceRequest serializer contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: DeploymentServiceRequest<*>
        try {
            parsed = JSON.decodeFromString(DeploymentServiceRequest.Serializer, deploymentServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The DeploymentServiceRequest serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
