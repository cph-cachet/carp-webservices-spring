package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.domain.users.RecruitmentSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * [RecruitmentSnapshotDeserializer] implements the deserialization logic for [RecruitmentSnapshot].
 */
class RecruitmentSnapshotDeserializer(private val validationMessages: MessageBase): JsonDeserializer<RecruitmentSnapshot>()
{
    companion object
    {
        /** The [LOGGER]. */
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [RecruitmentSnapshot] is blank or empty.
     * Also, if the [RecruitmentSnapshot] contains invalid format.
     * @return The deserialized [RecruitmentSnapshot] object.
     */
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): RecruitmentSnapshot
    {
        val recruitmentSnapshot: String
        try
        {
            recruitmentSnapshot = p?.codec?.readTree<TreeNode>(p).toString()

            if (!StringUtils.hasLength(recruitmentSnapshot))
            {
                LOGGER.error("The RecruitmentSnapshot cannot be blank or empty.")
                throw SerializationException(validationMessages.get("study.details.deserialization.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core RecruitmentSnapshot request contains bad format. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.details.deserialization.bad_format", ex.message.toString()))
        }

        val parsed: RecruitmentSnapshot
        try
        {
            parsed = JSON.decodeFromString(recruitmentSnapshot)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core RecruitmentSnapshot serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.details.deserialization.error", ex.message.toString()))
        }

        return parsed
    }
}