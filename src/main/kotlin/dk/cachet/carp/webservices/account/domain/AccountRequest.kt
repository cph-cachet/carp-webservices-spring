package dk.cachet.carp.webservices.account.domain

import dk.cachet.carp.webservices.security.authorization.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
data class AccountRequest(
    @field:NotBlank
    @field:Email
    val emailAddress: String,
    val role: Role,
)
