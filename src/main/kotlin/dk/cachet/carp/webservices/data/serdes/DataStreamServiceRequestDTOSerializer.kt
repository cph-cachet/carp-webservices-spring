// @file:Suppress("EXTERNAL_SERIALIZER_USELESS")

package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.data.domain.DataStreamServiceRequestDTO
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DataStreamServiceRequestDTOSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<DataStreamServiceRequestDTO>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        dataStreamServiceRequestDTO: DataStreamServiceRequestDTO?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (dataStreamServiceRequestDTO == null) {
            LOGGER.error("The dataStreamServiceRequestDTO value is null.")
            throw SerializationException(validationMessages.get("dataStream.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(DataStreamServiceRequestDTO.serializer(), DataStreamServiceRequestDTO())
        } catch (ex: Exception) {
            LOGGER.error("The DataStreamServiceRequestDTO serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamServiceRequestDTO.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
