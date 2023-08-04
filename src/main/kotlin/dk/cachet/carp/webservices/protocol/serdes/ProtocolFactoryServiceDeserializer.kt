package dk.cachet.carp.webservices.protocol.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

class ProtocolFactoryServiceDeserializer(private val validationMessages: MessageBase): JsonDeserializer<ProtocolFactoryServiceRequest<*>>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ProtocolFactoryServiceRequest<*>
    {
        val request: String
        try
        {
            request =  p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(request))
            {
                LOGGER.error("The core ProtocolFactoryServiceRequest cannot be blank or empty.")
                throw SerializationException(validationMessages.get("protocol.factory.deserialization.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core ProtocolFactoryServiceRequest contains bad format. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("protocol.factory.deserialization.bad_format", ex.message.toString()))
        }

        val parsed: ProtocolFactoryServiceRequest<*>
        try
        {
            parsed = JSON.decodeFromString(ProtocolFactoryServiceRequest.Serializer, request)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core ProtocolFactoryServiceRequest serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("protocol.factory.deserialization.error", ex.message.toString()))
        }

        return parsed
    }
}