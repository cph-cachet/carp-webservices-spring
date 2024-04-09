package dk.cachet.carp.webservices.security.authentication.domain

import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class Account(
    var id: String? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var role: Role? = null,
    var carpClaims: List<Claim>? = emptyList(),
) {
    companion object Factory {
        fun fromJwt(jwt: JwtAuthenticationToken): Account {
            val claims = jwt.token.claims
            val role = jwt.authorities.maxOfOrNull { Role.fromString(it.authority) }

            if (role == null || role == Role.UNKNOWN) {
                throw IllegalArgumentException("No CARP role found in JWT token.")
            }

            return Account(
                id = claims["sub"] as? String ?: "",
                username = claims["preferred_username"] as? String ?: "",
                firstName = claims["given_name"] as? String ?: "",
                lastName = claims["family_name"] as? String ?: "",
                email = claims["email"] as? String ?: "",
                role = role,
                carpClaims = claims.mapNotNull { Claim.fromTokenClaimObject(it.key to it.value) }
            )
        }

        fun fromAccountIdentity(identity: AccountIdentity): Account = when (identity) {
            is EmailAccountIdentity -> {
                Account( email = identity.emailAddress.address )
            }

            is UsernameAccountIdentity -> {
                Account( username = identity.username.name )
            }

            else -> {
                throw IllegalArgumentException("Unsupported account identity type: ${identity::class.simpleName}")
            }
        }
    }

    fun getIdentity(): AccountIdentity = when {
        !email.isNullOrBlank() -> EmailAccountIdentity(email!!)
        !username.isNullOrBlank() -> UsernameAccountIdentity(username!!)
        else -> throw IllegalArgumentException("Account should have an email or username.")
    }
}