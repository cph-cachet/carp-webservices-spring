package dk.cachet.carp.webservices.data.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

class DataStreamBatchDeserializer(private val validationMessages: MessageBase): JsonDeserializer<DataStreamBatch>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): DataStreamBatch
    {
        val dataStreamBatch: String
        try
        {
            dataStreamBatch =  p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(dataStreamBatch))
            {
                LOGGER.error("The dataStreamBatch cannot be blank or empty.")
                throw SerializationException(validationMessages.get("dataStreamConfig.deserialization.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The dataStreamBatch contains bad format. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("dataStreamBatch.deserialization.bad_format", ex.message.toString()))
        }

        val parsed: DataStreamBatch
        try
        {
            parsed = JSON.decodeFromString(dataStreamBatch)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core dataStreamBatch serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("dataStreamBatch.deserialization.error", ex.message.toString()))
        }

        return parsed
    }
}