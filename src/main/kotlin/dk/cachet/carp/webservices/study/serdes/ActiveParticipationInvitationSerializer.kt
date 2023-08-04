package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ActiveParticipationInvitationSerializer(private val validationMessages: MessageBase): JsonSerializer<ActiveParticipationInvitation>()
{

    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param activeParticipationInvitation The [ActiveParticipationInvitation] object containing the json object parsed.
     * @throws SerializationException If the [ActiveParticipationInvitation] is blank or empty.
     * Also, if the [ActiveParticipationInvitation] contains invalid format.
     * @return The serialization [ActiveParticipationInvitation] object.
     */
    override fun serialize(activeParticipationInvitation: ActiveParticipationInvitation?, jsonGenerator: JsonGenerator?, serializers: SerializerProvider?)
    {
        if (activeParticipationInvitation == null)
        {
            LOGGER.error("The ActiveParticipationInvitation is null.")
            throw SerializationException(validationMessages.get("study.active.participation.invitation.serialization.empty"))
        }

        val serialized: String
        try
        {
            serialized = JSON.encodeToString(activeParticipationInvitation)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The activeParticipationInvitation is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.active.participation.invitation.serialization.error", ex.message.toString()))
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}