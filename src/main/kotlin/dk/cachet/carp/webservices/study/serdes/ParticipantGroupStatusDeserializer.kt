package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [ParticipantGroupStatusDeserializer].
 * The [ParticipantGroupStatusDeserializer] implements the deserialization logic for [ParticipantGroupStatus].
 */
class ParticipantGroupStatusDeserializer(private val validationMessages: MessageBase): JsonDeserializer<ParticipantGroupStatus>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [ParticipantGroupStatus] is blank or empty.
     * Also, if the [ParticipantGroupStatus] contains invalid format.
     * @return The deserialized [ParticipantGroupStatus] object.
     */
    override fun deserialize(jsonParser: JsonParser?, context: DeserializationContext?): ParticipantGroupStatus
    {
        val participantGroupStatus: String
        try
        {
            participantGroupStatus =  jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(participantGroupStatus))
            {
                LOGGER.error("The ParticipantGroupStatus cannot be blank or empty.")
                throw SerializationException(validationMessages.get("study.participant.group.status.deserialization.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core ParticipantGroupStatus request contains bad format. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.participant.group.status.deserialization.bad_format", ex.message.toString()))
        }

        val parsed: ParticipantGroupStatus
        try
        {
            parsed = JSON.decodeFromString(participantGroupStatus)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core ParticipantGroupStatus serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.participant.group.status.deserialization.error", ex.message.toString()))
        }

        return parsed
    }
}