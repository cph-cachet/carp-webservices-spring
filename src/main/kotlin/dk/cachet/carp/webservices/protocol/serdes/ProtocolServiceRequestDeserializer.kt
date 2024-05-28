package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [ProtocolServiceRequestDeserializer].
 * [ProtocolServiceRequestDeserializer] implements the serialization logic for [ProtocolServiceRequest].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ProtocolServiceRequestDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<ProtocolServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [ProtocolServiceRequest] is blank or empty.
     * Also, if the [ProtocolServiceRequest] contains invalid format.
     * @return The deserialized [ProtocolServiceRequest] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): ProtocolServiceRequest<*> {
        val protocolServiceRequest: String
        try {
            protocolServiceRequest = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(protocolServiceRequest)) {
                LOGGER.error("The core ProtocolServiceRequest cannot be blank or empty.")
                throw SerializationException(validationMessages.get("protocol.service.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core ProtocolServiceRequest contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.service.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: ProtocolServiceRequest<*>
        try {
            parsed = JSON.decodeFromString(ProtocolServiceRequest.Serializer, protocolServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The core ProtocolServiceRequest serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.service.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
