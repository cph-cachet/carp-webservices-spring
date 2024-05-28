package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [DataStreamServiceRequestSerializer].
 * The [DataStreamServiceRequestSerializer] implements the deserialization logic for [DataStreamServiceRequest].
 */
class DataStreamServiceRequestSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<DataStreamServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param dataStreamServiceRequest The [DataStreamServiceRequest] object containing the json object parsed.
     * @param jsonGenerator The [jsonGenerator] to write JSON content generated from the deployment service request.
     * @param serializers The [serializers] to serialize the parsed object from the deployment  service request.
     * @throws SerializationException If the [DataStreamServiceRequest] is blank or empty.
     * Also, if the [DataStreamServiceRequest] contains invalid format.
     * @return The serialization of deployment service request object.
     */
    override fun serialize(
        dataStreamServiceRequest: DataStreamServiceRequest<*>?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (dataStreamServiceRequest == null) {
            LOGGER.error("The dataStreamServiceRequest value is null.")
            throw SerializationException(validationMessages.get("dataStream.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(DataStreamServiceRequest.Serializer, dataStreamServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The DataStreamServiceRequest serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamServiceRequest.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
