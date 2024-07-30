package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [DataStreamServiceRequestDeserializer].
 * The [DataStreamServiceRequestDeserializer] implements the deserialization logic for [DataStreamServiceRequest].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DataStreamServiceRequestDeserializer(
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
    ): DataStreamServiceRequest<*> {
        val dataStreamServiceRequest: String
        try {
            dataStreamServiceRequest = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(dataStreamServiceRequest)) {
                LOGGER.error("The DataStreamServiceRequest cannot be blank or empty.")
                throw SerializationException(validationMessages.get("dataStream.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The DataStreamServiceRequest deserializer contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStream.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: DataStreamServiceRequest<*>
        try {
            parsed = WS_JSON.decodeFromString(DataStreamServiceRequest.Serializer, dataStreamServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The DataStreamServiceRequest deserializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamServiceRequest.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
