package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WSInputDataTypes.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ParticipationServiceRequestDeserializer(private val validationMessages: MessageBase) :
    JsonDeserializer<ParticipationServiceRequest<*>>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ParticipationServiceRequest<*> {
        val participationServiceRequest: String
        try {
            participationServiceRequest = p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(participationServiceRequest)) {
                LOGGER.error("The ParticipationServiceRequest cannot be blank or empty.")
                throw SerializationException(
                    validationMessages.get("deployment.participation_service_request.deserialization.empty"),
                )
            }
        } catch (ex: Exception) {
            LOGGER.error("The ParticipationServiceRequest contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "deployment.participation_service_request.deserialization.bad_format",
                    ex.message.toString(),
                ),
            )
        }

        val parsed: ParticipationServiceRequest<*>
        try {
            parsed = WS_JSON.decodeFromString(ParticipationServiceRequest.Serializer, participationServiceRequest)
        } catch (ex: Exception) {
            LOGGER.error("The ParticipationServiceRequest is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "deployment.participation_service_request.deserialization.error",
                    ex.message.toString(),
                ),
            )
        }

        return parsed
    }
}
