package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dk.cachet.carp.studies.domain.users.RecruitmentSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.study.domain.Recruitment
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * The [RecruitmentSnapshotSerializer] implements the serialization logic for [Recruitment].
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class RecruitmentSnapshotSerializer(
    private val validationMessages: MessageBase,
) : JsonSerializer<RecruitmentSnapshot>() {
    companion object {
        /** The [LOGGER]. */
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [serialize] function is used to serialize the parsed object.
     * @param value The [Recruitment] object to be serialized.
     * @throws SerializationException If something goes wrong while serializing the [value].
     */
    override fun serialize(
        value: RecruitmentSnapshot?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?,
    ) {
        if (value == null) {
            LOGGER.error("The RecruitmentSnapshot is null.")
            throw SerializationException(validationMessages.get("study.details.serialization.empty"))
        }

        val serialized: String
        try {
            serialized = WS_JSON.encodeToString(value)
        } catch (ex: Exception) {
            LOGGER.error("The RecruitmentSnapshot is not valid. Exception: $ex")
            throw SerializationException(validationMessages.get("study.details.serialization.error"))
        }

        gen!!.writeRawValue(serialized)
    }
}
