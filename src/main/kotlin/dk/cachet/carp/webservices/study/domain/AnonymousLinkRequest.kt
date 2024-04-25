package dk.cachet.carp.webservices.study.domain

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive

/**
 * Request to generate anonymous participant links for a study.
 *
 * @param amountOfAccounts is hard capped at 100 as it cannot be guaranteed that the server would be able to respond
 * before the timeout if we were to generate more than that. The service method parallelizes requests to the auth
 * server, but the default CORE service implementation is synchronous, that is the bottleneck.
 */
data class AnonymousLinkRequest(
    @field:Positive
    @field:Max( 100 )
    val amountOfAccounts: Int,
    @field:Positive
    val expirationSeconds: Long,
    @field:NotEmpty
    val redirectUri: String,
    @field:NotBlank
    val participantRoleName: String
)
