package dk.cachet.carp.webservices.datastream.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.data.application.SyncPoint
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class SyncPointDeserializer(private val validationMessages: MessageBase) : JsonDeserializer<SyncPoint>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): SyncPoint {
        val syncPoint: String
        try {
            syncPoint = p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(syncPoint)) {
                LOGGER.error("The dataStreamServiceRequest.syncPoint cannot be blank or empty.")
                throw SerializationException(validationMessages.get("data.stream.syncPoint.deserialization.empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The dataStreamServiceRequest.syncPoint request contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("data.stream.syncPoint.deserialization.bad_format", ex.message.toString()),
            )
        }

        val parsed: SyncPoint
        try {
            parsed = WS_JSON.decodeFromString(syncPoint)
        } catch (ex: Exception) {
            LOGGER.error("The dataStreamServiceRequest.syncPoint deserializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("data.stream.syncPoint.deserialization.error", ex.message.toString()),
            )
        }

        return parsed
    }
}
