package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.data.domain.DataStreamServiceRequestDTO
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [DataStreamServiceRequestDTODeserializer].
 * The [DataStreamServiceRequestDTODeserializer] implements the deserialization logic for [DataStreamServiceRequestDTO].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DataStreamServiceRequestDTODeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<DataStreamServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [DeploymentServiceRequest] is blank or empty.
     * Also, if the [DataStreamServiceRequest] contains invalid format.
     * @return The deserialized deployment service request object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): DataStreamServiceRequest<*>? {
        val dataStreamServiceRequestDTO: String
        try {
            dataStreamServiceRequestDTO = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(dataStreamServiceRequestDTO)) {
                LOGGER.error("The DataStreamServiceRequestDTO cannot be blank or empty.")
                throw SerializationException(validationMessages.get("dataStream.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The DataStreamServiceRequestDTO deserializer contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStream.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: DataStreamServiceRequestDTO
        try {
            parsed = JSON.decodeFromString(dataStreamServiceRequestDTO)
        } catch (ex: Exception) {
            LOGGER.error("The DataStreamServiceRequestDTO deserializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamServiceRequest.deserialization.error", ex.message.toString()),
            )
        }

        return parsed.toDataStreamServiceRequest()
    }
}

/*
val parsed: DataStreamBatch
try {
    parsed = JSON.decodeFromString(dataStreamBatch)*/
