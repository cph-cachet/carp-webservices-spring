package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ProtocolFactoryServiceSerializer(private val validationMessages: MessageBase) : JsonSerializer<ProtocolFactoryServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        value: ProtocolFactoryServiceRequest<*>?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The core ProtocolFactoryServiceRequest is null.")
            throw SerializationException(validationMessages.get("protocol.factory.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(ProtocolFactoryServiceRequest.Serializer, value)
        } catch (ex: Exception) {
            LOGGER.error("The core DeviceRegistration is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("protocol.factory.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
