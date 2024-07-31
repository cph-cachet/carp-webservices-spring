package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DataStreamsConfigurationDeserializer(private val validationMessages: MessageBase) :
    JsonDeserializer<DataStreamsConfiguration>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): DataStreamsConfiguration {
        val dataStreamConfig: String
        try {
            dataStreamConfig = p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(dataStreamConfig)) {
                LOGGER.error("The DataStreamConfiguration cannot be blank or empty.")
                throw SerializationException(validationMessages.get("dataStreamConfig.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core dataStream request contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamConfig.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: DataStreamsConfiguration
        try {
            parsed = WS_JSON.decodeFromString(dataStreamConfig)
        } catch (ex: Exception) {
            LOGGER.error("The core DataStreamConfiguration serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("dataStreamConfig.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
