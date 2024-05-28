package dk.cachet.carp.webservices.account.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StringUtils

/**
 * The Class [AccountIdentityDeserializer].
 * The [AccountIdentityDeserializer] implements the deserialization logic for [AccountIdentity].
 */
class AccountIdentityDeserializer(private val validationMessage: MessageBase) : JsonDeserializer<AccountIdentity>() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [deserialize] is used to deserialize the parsed object.
     *
     * @param jsonParser The [jsonParser] object containing the json object parsed.
     * @param deserializationContext The [deserializationContext] object containing the context of the deserialization.
     * @throws SerializationException If the [AccountIdentity] is blank or empty.
     * Also, if the [AccountIdentity] contains invalid format.
     * @return The deserialized account object.
     */
    override fun deserialize(
        jsonParser: JsonParser?,
        deserializationContext: DeserializationContext?,
    ): AccountIdentity {
        val accountIdentity: String
        try {
            accountIdentity = jsonParser?.codec?.readTree<TreeNode>(jsonParser).toString()

            if (!StringUtils.hasLength(accountIdentity)) {
                LOGGER.error("The core [AccountIdentity] request cannot be blank or empty.")
                throw SerializationException(validationMessage.get("account.identity.request-blank-or-empty"))
            }
        } catch (ex: Exception) {
            LOGGER.error("The core [AccountIdentity] request contains bad format. Exception: $ex")
            throw SerializationException(validationMessage.get("account.identity.request-bad-format"))
        }

        val parsed: AccountIdentity
        try {
            parsed = JSON.decodeFromString(accountIdentity)
        } catch (ex: Exception) {
            LOGGER.error("The core [AccountIdentity] serializer is not valid. Exception: $ex")
            throw SerializationException(validationMessage.get("account.identity.request-deserialization-not-valid"))
        }

        return parsed
    }
}
