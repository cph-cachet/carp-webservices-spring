package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WSInputDataTypes.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [RecruitmentServiceRequestSerializer].
 * [RecruitmentServiceRequestSerializer] implements the serialization logic for [RecruitmentServiceRequest].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class RecruitmentServiceRequestSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<RecruitmentServiceRequest<*>>() {
    companion object {
        /** The [LOGGER]. */
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param value The [RecruitmentServiceRequest] object containing the json object parsed.
     * @throws SerializationException If the [value] is blank or empty. Also, if the [value] contains invalid format.
     * @return The serialization of [RecruitmentServiceRequest] object.
     */
    override fun serialize(
        value: RecruitmentServiceRequest<*>?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The RecruitmentServiceRequest is null.")
            throw SerializationException(validationMessages.get("study.details.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(RecruitmentServiceRequest.Serializer, value)
        } catch (ex: Exception) {
            LOGGER.error("The RecruitmentServiceRequest is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("study.details.serialization.error"))
        }

        gen!!.writeRawValue(serialized)
    }
}
