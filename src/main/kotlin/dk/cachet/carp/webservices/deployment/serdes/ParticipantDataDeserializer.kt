package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [ParticipantDataDeserializer].
 * [ParticipantDataDeserializer] implements the deserialization logic for [ParticipantData].
 */
class ParticipantDataDeserializer(private val validationMessages: MessageBase): JsonDeserializer<ParticipantData>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param p The [JsonParser] object containing the json object parsed.
     * @param ctxt The [DeserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [ParticipantData] is blank or empty.
     * Also, if the [ParticipantData] contains invalid format.
     * @return The deserialized study deployment snapshot object.
     */
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ParticipantData
    {
        val participantData: String
        try
        {
            participantData =  p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(participantData))
            {
                LOGGER.error("The core ParticipantData cannot be blank or empty.")
                throw SerializationException(validationMessages.get("deployment.participant_data.deserialization.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core ParticipantData contains bad format. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("deployment.participant_data.deserialization.bad_format", ex.message.toString()))
        }

        val parsed: ParticipantData
        try
        {
            parsed = JSON.decodeFromString(participantData)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core ParticipantData is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("deployment.participant_data.deserialization.error", ex.message.toString()))
        }

        return parsed
    }
}