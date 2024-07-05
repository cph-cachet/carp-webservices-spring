package dk.cachet.carp.webservices.deployment.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The Class [ParticipantGroupSnapshotSerializer].
 * The [ParticipantGroupSnapshotSerializer] implements the serialization logic for [ParticipantGroupSnapshot].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class ParticipantGroupSnapshotSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<ParticipantGroupSnapshot>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     *
     * @param value The [ParticipantGroupSnapshot] object containing the json object parsed.
     * @param gen The [JsonGenerator] to write JSON content generated from the study deployment snapshot.
     * @param serializers The [SerializerProvider] to serialize the parsed object from the study deployment snapshot.
     * @throws SerializationException If the [ParticipantGroupSnapshot] is blank or empty.
     * Also, if the [ParticipantGroupSnapshot] contains invalid format.
     * @return The serialization of deployment service request object.
     */
    override fun serialize(
        value: ParticipantGroupSnapshot?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The core ParticipantGroupSnapshot is null.")
            throw SerializationException(
                validationMessages.get("deployment.participant_group_snapshot.serialization.empty"),
            )
        }

        val serialized: String
        try {
            serialized = JSON.encodeToString(value)
        } catch (ex: Exception) {
            LOGGER.error("The core ParticipantGroupSnapshot is not valid. Exception: ${ex.message}")
            throw SerializationException(
                validationMessages.get(
                    "deployment.participant_group_snapshot.serialization.error",
                    ex.message.toString(),
                ),
            )
        }

        gen!!.writeRawValue(serialized)
    }
}
