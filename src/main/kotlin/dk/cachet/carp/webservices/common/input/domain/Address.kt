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
    val address1: String?,
    val address2: String?,
    val street: String?,
    val city: String?,
    val postalCode: String?,
    val country: String?,
) : Data
