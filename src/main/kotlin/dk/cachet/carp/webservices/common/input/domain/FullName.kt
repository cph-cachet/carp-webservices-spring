package dk.cachet.carp.webservices.common.input.domain

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.webservices.common.input.WSInputDataTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a full name of a participant.
 */
@Serializable
@SerialName(WSInputDataTypes.FULL_NAME_TYPE_NAME)
data class FullName(
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
) : Data
