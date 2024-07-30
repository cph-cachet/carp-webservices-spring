package dk.cachet.carp.webservices.common.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(WSInputDataTypes.SSN_TYPE_NAME)
data class SocialSecurityNumber(
    /**
     * The social security number (SSN)
     */
    val socialSecurityNumber: String,
    /**
     * The country in which this [socialSecurityNumber] was issued.
     */
    val country: String,
) : Data
