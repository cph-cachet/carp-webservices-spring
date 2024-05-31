package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ProtocolVersionSerializer(private val validationMessages: MessageBase) : JsonSerializer<ProtocolVersion>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        protocolVersion: ProtocolVersion?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (protocolVersion == null) {
            LOGGER.error("ProtocolVersion is null.")
            throw SerializationException(validationMessages.get("protocol.factory.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(protocolVersion)
        } catch (ex: Exception) {
            LOGGER.error("The protocolVersion is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.factory.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
