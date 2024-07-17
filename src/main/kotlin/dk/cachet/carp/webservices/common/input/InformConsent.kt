package dk.cachet.carp.webservices.common.input

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable( with = InformedConsentSerializer::class )
@SerialName( "InformedConsent" )
class InformConsent (
    /// The time this informed consent was signed.
    val signedTimestamp: Instant,

    /// The location where this informed consent was signed.
    val signedLocation: String?,

    /// The ID of the participant who signed this consent.
    val userId: String?,

    /// The full name of the participant who signed this consent.
    name: String,

    /// The content of the signed consent.
    ///
    /// This may be plain text or JSON.
    val consent:  String?,

    /// The image of the provided signature in png format as bytes.
    val signatureImage: String? )
{
}

class InformedConsentSerializer : KSerializer<InformConsent>
{
    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): InformConsent {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: InformConsent) {
        TODO("Not yet implemented")
    }
}