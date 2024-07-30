package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ActiveParticipationInvitationSerializer(private val validationMessages: MessageBase) :
    JsonSerializer<ActiveParticipationInvitation>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun serialize(
        activeParticipationInvitation: ActiveParticipationInvitation?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (activeParticipationInvitation == null) {
            LOGGER.error("The ActiveParticipationInvitation is null.")
            throw SerializationException(
                validationMessages.get("study.active.participation.invitation.serialization.empty"),
            )
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(activeParticipationInvitation)
        } catch (ex: Exception) {
            LOGGER.error("The activeParticipationInvitation is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "study.active.participation.invitation.serialization.error",
                    ex.message.toString(),
                ),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
