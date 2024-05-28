package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.application.SyncPoint
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class SyncPointSerializer(private val validationMessages: MessageBase) : JsonSerializer<SyncPoint>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        value: SyncPoint?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The SyncPoint is null.")
            throw SerializationException(validationMessages.get("data.stream.syncPoint.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(value)
        } catch (ex: Exception) {
            LOGGER.error("The syncPoint request is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("data.stream.syncPoint.serialization.error", ex.message.toString()),
            )
        }

        gen!!.writeRawValue(serialized)
    }
}
