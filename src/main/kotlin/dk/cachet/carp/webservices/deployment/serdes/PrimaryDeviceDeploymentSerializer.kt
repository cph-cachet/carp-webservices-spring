package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [PrimaryDeviceDeploymentSerializer].
 * The [PrimaryDeviceDeploymentSerializer] implements the serialization logic for [PrimaryDeviceDeployment].
 */
class PrimaryDeviceDeploymentSerializer(private val validationMessages: MessageBase): JsonSerializer<PrimaryDeviceDeployment>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param primaryDeviceDeployment The [primaryDeviceDeployment] object containing the json object parsed.
     * @param jsonGenerator The [jsonGenerator] to write JSON content generated from the study deployment snapshot.
     * @param serializers The [serializers] to serialize the parsed object from the study deployment snapshot.
     * @throws SerializationException If the [PrimaryDeviceDeployment] is blank or empty.
     * Also, if the [PrimaryDeviceDeployment] contains invalid format.
     * @return The serialization of deployment service request object.
     */
    override fun serialize(primaryDeviceDeployment: PrimaryDeviceDeployment?, jsonGenerator: JsonGenerator?, serializers: SerializerProvider?)
    {
        if (primaryDeviceDeployment == null)
        {
            LOGGER.error("The MasterDeviceDeployment value is null.")
            throw SerializationException(validationMessages.get("deployment.master_device.serialization.empty"))
        }

        val serialized: String
        try
        {
            serialized = JSON.encodeToString(primaryDeviceDeployment)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The MasterDeviceDeployment is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("deployment.master_device.serialization.error", ex.message.toString()))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}