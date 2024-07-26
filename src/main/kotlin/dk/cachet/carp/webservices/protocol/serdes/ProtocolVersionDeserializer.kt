package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WSInputDataTypes.WS_JSON
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ProtocolVersionDeserializer(private val validationMessages: MessageBase) : JsonDeserializer<ProtocolVersion>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [StudyProtocolSnapshot] is blank or empty.
     * Also, if the [StudyProtocolSnapshot] contains invalid format.
     * @return The deserialized [StudyProtocolSnapshot] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): ProtocolVersion {
        val protocolVersion: String
        try {
            protocolVersion = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(protocolVersion)) {
                LOGGER.error("The core StudyProtocolSnapshot cannot be blank or empty.")
                throw SerializationException(validationMessages.get("protocol.snapshot.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core StudyProtocolSnapshot contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.snapshot.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: ProtocolVersion
        try {
            parsed = WS_JSON.decodeFromString(protocolVersion)
        } catch (ex: Exception) {
            LOGGER.error("The core StudyProtocolSnapshot serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.snapshot.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
