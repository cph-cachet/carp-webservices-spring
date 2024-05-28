package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ActiveParticipationInvitationDeserializer(private val validationMessages: MessageBase) :
    JsonDeserializer<ActiveParticipationInvitation>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [ActiveParticipationInvitation] is blank or empty.
     * Also, if the [ActiveParticipationInvitation] contains invalid format.
     * @return The deserialized [ActiveParticipationInvitation] object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        context: DeserializationContext?,
    ): ActiveParticipationInvitation {
        val activeParticipationInvitation: String
        try {
            activeParticipationInvitation = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(activeParticipationInvitation)) {
                LOGGER.error("The ActiveParticipationInvitation cannot be blank or empty.")
                throw SerializationException(
                    validationMessages.get("study.active.participation.invitation.deserialization.empty"),
                )
            }
        } catch (ex: Exception) {
            LOGGER.error("The ActiveParticipationInvitation request contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.active.participation.invitation.bad_format", ex.message.toString()),
            )
        }

        val parsed: ActiveParticipationInvitation
        try {
            parsed = JSON.decodeFromString(activeParticipationInvitation)
        } catch (ex: Exception) {
            LOGGER.error("The core ActiveParticipationInvitation serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "study.active.participation.invitation.deserialization.error",
                    ex.message.toString(),
                ),
            )
        }

        return parsed
    }
}
