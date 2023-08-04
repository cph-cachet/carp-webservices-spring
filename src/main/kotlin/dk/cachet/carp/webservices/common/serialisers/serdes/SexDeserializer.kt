package dk.cachet.carp.webservices.common.serialisers.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.serialization.PolymorphicEnumSerializer
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

class SexDeserializer(private val validationMessages: MessageBase): JsonDeserializer<Sex>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [deserialize] is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [Sex] is blank or empty.
     * Also, if the [Sex] contains invalid format.
     * @return The deserialized account object.
     */
    override fun deserialize(jsonParser: JsonParser?, deserializationContext: DeserializationContext?): Sex
    {
        val sex: String
        try
        {
            sex = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(sex))
            {
                LOGGER.error("The core Sex cannot be blank or empty!")
                throw SerializationException(validationMessages.get("deserialization.sex.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core Sex contains bad format. Exception: $ex")
            throw SerializationException(validationMessages.get("deserialization.sex.bad_format", ex.message.toString()))
        }

        val parsed: Sex
        try
        {
            parsed = JSON.decodeFromString(PolymorphicEnumSerializer( Sex.serializer() ), sex)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core Sex is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("deserialization.sex.error", ex.message.toString()))
        }

        return parsed
    }
}