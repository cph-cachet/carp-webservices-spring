package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [ProtocolServiceRequestSerializer].
 * [ProtocolServiceRequestSerializer] implements the serialization logic for [ProtocolServiceRequest].
 */
class ProtocolServiceRequestSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<ProtocolServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        protocolServiceRequest: ProtocolServiceRequest<*>?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (protocolServiceRequest == null) {
            LOGGER.error("The core ProtocolServiceRequest is null.")
            throw SerializationException(validationMessages.get("protocol.service.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(ProtocolServiceRequest.Serializer, protocolServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The core ProtocolServiceRequest is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("protocol.service.serialization.error"))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
