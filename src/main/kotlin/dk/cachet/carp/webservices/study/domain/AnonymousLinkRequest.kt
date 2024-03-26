package dk.cachet.carp.webservices.study.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive

data class AnonymousLinkRequest(
    @field:Positive
    val amountOfAccounts: Int,
    @field:Positive
    val expirationSeconds: Long,
    @field:NotEmpty
    val redirectUri: String,
    @field:NotBlank
    val participantRoleName: String
)
