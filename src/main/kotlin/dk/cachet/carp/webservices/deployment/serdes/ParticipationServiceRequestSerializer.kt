package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WSInputDataTypes.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ParticipationServiceRequestSerializer(private val validationMessages: MessageBase) :
    JsonSerializer<ParticipationServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        value: ParticipationServiceRequest<*>?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The ParticipationServiceRequest value is null.")
            throw SerializationException(
                validationMessages.get("deployment.participation_service_request.serialization.empty"),
            )
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(ParticipationServiceRequest.Serializer, value)
        } catch (ex: Exception) {
            LOGGER.error("The ParticipationServiceRequest is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "deployment.participation_service_request.serialization.error",
                    ex.message.toString(),
                ),
            )
        }

        gen!!.writeRawValue(serialized)
    }
}
