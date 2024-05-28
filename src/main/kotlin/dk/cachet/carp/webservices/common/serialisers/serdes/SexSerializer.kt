package dk.cachet.carp.webservices.common.serialisers.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.application.data.input.Sex
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.common.infrastructure.serialization.PolymorphicEnumSerializer
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [SexSerializer].
 * The [SexSerializer] implements the serialization mechanism for the [Sex] class.e
 */
class SexSerializer(private val validationMessages: MessageBase) : JsonSerializer<Sex>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [serialize] used to serialize the core [sex].
     * */
    override fun serialize(
        sex: Sex?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (sex == null) {
            LOGGER.error("The core Sex is null.")
            throw SerializationException(validationMessages.get("serialization.sex.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(PolymorphicEnumSerializer(Sex.serializer()), sex)
        } catch (ex: Exception) {
            LOGGER.error("The core Sex is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("serialization.sex.bad_format", ex.message.toString()))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
