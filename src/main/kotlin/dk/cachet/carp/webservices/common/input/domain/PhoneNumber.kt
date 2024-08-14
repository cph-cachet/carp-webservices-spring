package dk.cachet.carp.webservices.common.input.domain

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.webservices.common.input.WSInputDataTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a phone number of a participant.
 */
@Serializable
@SerialName(WSInputDataTypes.PHONE_NUMBER_TYPE_NAME)
data class PhoneNumber(
    /**
     * The country code of this phone number.
     *
     * The country code is represented by a string, since some country codes
     * contain a '-'. For example, "1-246" for Barbados or "44-1481" for Guernsey.
     * See [link](https://countrycode.org/)
     * or [link](https://en.wikipedia.org/wiki/List_of_country_calling_codes)
     */
    val countryCode: String,
    /**
     *  The ICO 3166 code of the [countryCode], if available.
     *    See https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes
     */
    val icoCode: String? = null,
    /**
     *  The phone [number].
     *  The phone number is represented as a string since it may be pretty-printed
     *  with spaces.
     */
    val number: String,
) : Data
