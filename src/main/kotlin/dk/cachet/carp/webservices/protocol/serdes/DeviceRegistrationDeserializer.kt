package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [DeviceRegistrationDeserializer].
 * The [DeviceRegistrationDeserializer] implements the serialization logic for [DeviceRegistration].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DeviceRegistrationDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<DeviceRegistration>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [DeviceRegistration] is blank or empty.
     * Also, if the [DeviceRegistration] contains invalid format.
     * @return The deserialized [DeviceRegistration] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): DeviceRegistration {
        val deviceRegistration: String
        try {
            deviceRegistration = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(deviceRegistration)) {
                LOGGER.error("The core DeviceRegistration cannot be blank or empty.")
                throw SerializationException(validationMessages.get("protocol.device_reg.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core DeviceRegistration contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.device_reg.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: DeviceRegistration
        try {
            parsed = WS_JSON.decodeFromString(deviceRegistration)
        } catch (ex: Exception) {
            LOGGER.error("The core DeviceRegistration serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.device_reg.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
