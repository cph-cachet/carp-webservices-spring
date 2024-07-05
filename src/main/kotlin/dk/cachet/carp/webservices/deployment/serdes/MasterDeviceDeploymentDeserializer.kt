package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [MasterDeviceDeploymentDeserializer].
 * The [MasterDeviceDeploymentDeserializer] implements the deserialization logic for [MasterDeviceDeployment].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class MasterDeviceDeploymentDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<PrimaryDeviceDeployment>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [PrimaryDeviceDeployment] is blank or empty.
     * Also, if the [PrimaryDeviceDeployment] contains invalid format.
     * @return The deserialized deployment service request object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): PrimaryDeviceDeployment {
        val masterDeviceDeployment: String
        try {
            masterDeviceDeployment = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(masterDeviceDeployment)) {
                LOGGER.error("The MasterDeviceDeployment cannot be blank or empty.")
                throw SerializationException(validationMessages.get("deployment.master_device.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The MasterDeviceDeployment contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.master_device.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: PrimaryDeviceDeployment
        try {
            parsed = JSON.decodeFromString(masterDeviceDeployment)
        } catch (ex: Exception) {
            LOGGER.error("The MasterDeviceDeployment is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("deployment.master_device.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
