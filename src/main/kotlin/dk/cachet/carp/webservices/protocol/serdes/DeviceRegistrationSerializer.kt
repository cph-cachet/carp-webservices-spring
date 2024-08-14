package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [DeviceRegistrationSerializer].
 * The [DeviceRegistrationSerializer] implements the serialization logic for [DeviceRegistration].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DeviceRegistrationSerializer(private val validationMessages: MessageBase) : JsonSerializer<DeviceRegistration>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param deviceRegistration The [deviceRegistration] object containing the json object parsed.
     * @param jsonGenerator The [jsonGenerator] to write JSON content generated from the device registration.
     * @param serializers The [serializers] to serialize the parsed object from the device registration.
     * @throws SerializationException If the [StudyProtocolSnapshot] is blank or empty.
     * Also, if the [StudyProtocolSnapshot] contains invalid format.
     * @return The serialization [StudyProtocolSnapshot] object.
     */
    override fun serialize(
        deviceRegistration: DeviceRegistration?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (deviceRegistration == null) {
            LOGGER.error("The core DeviceRegistration is null.")
            throw SerializationException(validationMessages.get("protocol.device_reg.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(deviceRegistration)
        } catch (ex: Exception) {
            LOGGER.error("The core DeviceRegistration is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.device_reg.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
