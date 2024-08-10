package dk.cachet.carp.webservices.common.input.domain

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.webservices.common.input.WSInputDataTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a full address of a participant.
 */
@Serializable
@SerialName(WSInputDataTypes.ADDRESS_TYPE_NAME)
data class Address(
    val address1: String? = null,
    val address2: String? = null,
    val street: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
) : Data
