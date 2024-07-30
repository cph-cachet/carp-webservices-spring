package dk.cachet.carp.webservices.common.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a full name of a participant.
 */
@Serializable
@SerialName(WSInputDataTypes.FULL_NAME_TYPE_NAME)
data class FullName(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
) : Data
