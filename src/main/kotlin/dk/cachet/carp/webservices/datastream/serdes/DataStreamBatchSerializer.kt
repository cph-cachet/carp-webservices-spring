package dk.cachet.carp.webservices.datastream.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamBatchSerializer
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DataStreamBatchSerializer(private val validationMessages: MessageBase) : JsonSerializer<DataStreamBatch>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        value: DataStreamBatch?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The DataStreamBatch value is null.")
            throw SerializationException(validationMessages.get("dataStreamBatch.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(DataStreamBatchSerializer, value)
        } catch (ex: Exception) {
            LOGGER.error("The dataStreamBatch is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamBatch.serialization.error", ex.message.toString()),
            )
        }

        gen!!.writeRawValue(serialized)
    }
}
