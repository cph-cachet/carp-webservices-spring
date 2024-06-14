package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.data.domain.DataStreamServiceRequestDTO
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DataStreamServiceRequestDTODeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<DataStreamServiceRequestDTO>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    private val json = Json { ignoreUnknownKeys = true }

    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): DataStreamServiceRequestDTO {
        val jsonNode = jsonParser!!.codec.readTree<JsonNode>(jsonParser)

        try {
            return json.decodeFromString(DataStreamServiceRequestDTO.serializer(), jsonNode.toString())
        } catch (ex: kotlinx.serialization.SerializationException) {
            LOGGER.error(
                "Serialization error in DataStreamServiceRequestDTO deserializer. Exception: ${ex.message}",
                ex,
            )
            throw org.apache.commons.lang3.SerializationException(
                validationMessages.get("dataStreamServiceRequestDTO.deserialization.error", ex.message.toString()),
                ex,
            )
        } catch (ex: IllegalArgumentException) {
            LOGGER.error("Invalid argument in DataStreamServiceRequestDTO deserializer. Exception: ${ex.message}", ex)
            throw org.apache.commons.lang3.SerializationException(
                validationMessages.get("dataStreamServiceRequestDTO.deserialization.error", ex.message.toString()),
                ex,
            )
        }
    }
}
