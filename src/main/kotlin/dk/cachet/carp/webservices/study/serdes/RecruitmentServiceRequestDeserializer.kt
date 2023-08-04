package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils


/**
 * [RecruitmentServiceRequestDeserializer] implements the deserialization logic for [RecruitmentServiceRequest].
 */
class RecruitmentServiceRequestDeserializer(private val validationMessages: MessageBase): JsonDeserializer<RecruitmentServiceRequest<*>>()
{

    companion object {
        /** The [LOGGER]. */
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [RecruitmentServiceRequest] is blank or empty.
     * Also, if the [RecruitmentServiceRequest] contains invalid format.
     * @return The deserialized [RecruitmentServiceRequest] object.
     */

    override fun deserialize(jsonParser: JsonParser?, ctxt: DeserializationContext?): RecruitmentServiceRequest<*>
    {
            val recruitmentServiceRequest: String
            try {
                recruitmentServiceRequest = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

                if (!StringUtils.hasLength(recruitmentServiceRequest)) {
                    LOGGER.error("The RecruitmentServiceRequest cannot be blank or empty.")
                    throw SerializationException(validationMessages.get("study.details.deserialization.empty"))
                }
            } catch (ex: Exception) {
                LOGGER.error("The core RecruitmentServiceRequest request contains bad format. Exception: ${ex.message}")
                throw SerializationException(
                    validationMessages.get(
                        "study.details.deserialization.bad_format",
                        ex.message.toString()
                    )
                )
            }

            val parsed: RecruitmentServiceRequest<*>
            try {
                parsed = JSON.decodeFromString(RecruitmentServiceRequest.Serializer, recruitmentServiceRequest)
            } catch (ex: Exception) {
                LOGGER.error("The core RecruitmentServiceRequest serializer is not valid. Exception: ${ex.message}")
                throw SerializationException(
                    validationMessages.get(
                        "study.details.deserialization.error",
                        ex.message.toString()
                    )
                )
            }

            return parsed
        }
}
