package dk.cachet.carp.webservices.study.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [StudyServiceRequestDeserializer].
 * The [StudyServiceRequestDeserializer] implements the deserialization logic for [StudyServiceRequest].
 */
class StudyServiceRequestDeserializer(private val validationMessages: MessageBase): JsonDeserializer<StudyServiceRequest<*>>()
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [deserialize] function is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @throws SerializationException If the [StudyServiceRequest] is blank or empty.
     * Also, if the [StudyServiceRequest] contains invalid format.
     * @return The deserialized [StudyServiceRequest] object.
     */
    override fun deserialize(jsonParser: JsonParser?, context: DeserializationContext?): StudyServiceRequest<*>
    {
        val studyServiceRequest: String
        try
        {
            studyServiceRequest =  jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(studyServiceRequest))
            {
                LOGGER.error("The StudyServiceRequest cannot be blank or empty.")
                throw SerializationException(validationMessages.get("study.service.deserialization.empty"))
            }
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core study request contains bad format. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.service.deserialization.bad_format", ex.message.toString()))
        }

        val parsed: StudyServiceRequest<*>
        try
        {
            parsed = JSON.decodeFromString(StudyServiceRequest.Serializer, studyServiceRequest)
        }
        catch (ex: Exception)
        {
            LOGGER.error("The core study serializer is not valid. Exception: ${ex.message}")
            throw SerializationException(validationMessages.get("study.service.deserialization.error", ex.message.toString()))
        }

        return parsed
    }
}