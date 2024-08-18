package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [ServiceRequestSerializer].
 * The [ServiceRequestSerializer] implements the serialization logic for [ApplicationServiceRequest].
 * It's the Jackson JsonSerializer wrapper for core [ApplicationServiceRequest] serializer.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ServiceRequestSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<ApplicationServiceRequest<*, *>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        serviceRequest: ApplicationServiceRequest<*, *>,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        try {
            WS_JSON.encodeToString(serviceRequest.getResponseSerializer())
        } catch (e: Exception) {
            LOGGER.error("Error occurred while serializing the StudyServiceRequest: ${e.message}")
            throw SerializationException(validationMessages.get("study.service.serialization.invalid"))
        }
    }
}
