package dk.cachet.carp.webservices.study.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class MagicLinkRequest(
    @field:NotBlank
    val studyId: String,
    @field:Positive
    val numberOfAccounts: Number,

    val expiryDate: String

)
