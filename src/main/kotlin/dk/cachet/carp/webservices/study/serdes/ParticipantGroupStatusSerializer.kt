package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [ParticipantGroupStatusSerializer].
 * [ParticipantGroupStatusSerializer] implements the serialization logic for [ParticipantGroupStatus].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ParticipantGroupStatusSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<ParticipantGroupStatus>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param participantGroupStatus The [participantGroupStatus] object containing the json object parsed.
     * @throws SerializationException If the [ParticipantGroupStatus] is blank or empty.
     * Also, if the [ParticipantGroupStatus] contains invalid format.
     * @return The serialization [ParticipantGroupStatus] object.
     */
    override fun serialize(
        participantGroupStatus: ParticipantGroupStatus?,
        jsonGenerator: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (participantGroupStatus == null) {
            LOGGER.error("The ParticipantGroupStatus is null.")
            throw SerializationException(validationMessages.get("study.participant.group.status.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(participantGroupStatus)
        } catch (ex: Exception) {
            LOGGER.error("The ParticipantGroupStatus is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get("study.participant.group.status.serialization.error", ex.message.toString()),
            )
        }

        jsonGenerator!!.writeRawValue(serialized)
    }
}
