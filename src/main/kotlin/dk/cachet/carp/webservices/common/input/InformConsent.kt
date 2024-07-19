package dk.cachet.carp.webservices.common.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InformedConsent")
data class InformConsent(
    // / The time this informed consent was signed.
    val signedTimestamp: Instant = Clock.System.now(),
    // / The location where this informed consent was signed.
    val signedLocation: String? = null,
    // / The ID of the participant who signed this consent.
    val userId: String?,
    // / The full name of the participant who signed this consent.
    val name: String,
    // / The content of the signed consent.
    // /
    // / This may be plain text or JSON.
    val consent: String?,
    // / The image of the provided signature in png format as bytes.
    val signatureImage: String?,
) : Data
