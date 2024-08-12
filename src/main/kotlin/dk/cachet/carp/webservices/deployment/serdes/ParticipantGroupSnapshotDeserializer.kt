package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [ParticipantGroupSnapshotDeserializer].
 * [ParticipantGroupSnapshotDeserializer] implements the deserialization logic for [ParticipantGroupSnapshot].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ParticipantGroupSnapshotDeserializer(
    private val validationMessages: MessageBase,
) : JsonDeserializer<ParticipantGroupSnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param p The [JsonParser] object containing the json object parsed.
     * @param ctxt The [DeserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [ParticipantGroupSnapshot] is blank or empty.
     * Also, if the [ParticipantGroupSnapshot] contains invalid format.
     * @return The deserialized study deployment snapshot object.
     */
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?,
    ): ParticipantGroupSnapshot {
        val participantGroupSnapshot: String
        try {
            participantGroupSnapshot = p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(participantGroupSnapshot)) {
                LOGGER.error("The core ParticipantGroupSnapshot cannot be blank or empty.")
                throw SerializationException(
                    validationMessages.get("deployment.participant_group_snapshot.deserialization.empty"),
                )
            }
        } catch (ex: Exception) {
            LOGGER.error("The core ParticipantGroupSnapshot contains bad format. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "deployment.participant_group_snapshot.deserialization.bad_format",
                    ex.message.toString(),
                ),
            )
        }

        val parsed: ParticipantGroupSnapshot
        try {
            parsed = WS_JSON.decodeFromString(participantGroupSnapshot)
        } catch (ex: Exception) {
            LOGGER.error("The core ParticipantGroupSnapshot is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "deployment.participant_group_snapshot.deserialization.error",
                    ex.message.toString(),
                ),
            )
        }

        return parsed
    }
}
