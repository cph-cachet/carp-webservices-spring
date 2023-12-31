package dk.cachet.carp.webservices.security.authentication.domain

import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.KeycloakFacade
import dk.cachet.carp.webservices.security.authorization.Role
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

data class Account(
    var id: String? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var role: Role? = null,
)

@Service
class AccountFactory(
    private val issuerFacade: IssuerFacade
) {

    fun fromJwtAuthenticationToken(jwt: JwtAuthenticationToken): Account =
        when (issuerFacade) {
            is KeycloakFacade -> {
                val claims = jwt.token.claims
                val role = jwt.authorities.maxOfOrNull { Role.fromString(it.authority) }

                if (role == null || role == Role.UNKNOWN) {
                    throw IllegalArgumentException("No CARP role found in JWT token.")
                }

                Account(
                    id = claims["sub"] as String,
                    username = claims["preferred_username"] as String,
                    firstName = claims["given_name"] as String,
                    lastName = claims["family_name"] as String,
                    email = claims["email"] as String,
                    role = role
                )
            }

            else -> {
                throw IllegalArgumentException("Unsupported issuer  type: ${issuerFacade::class.simpleName}")
            }
        }

    fun fromAccountIdentity(identity: AccountIdentity): Account = when (identity) {
        is EmailAccountIdentity -> {
            Account(email = identity.emailAddress.address, username = identity.emailAddress.address)
        }

        else -> {
            throw IllegalArgumentException("Unsupported account identity type: ${identity::class.simpleName}")
        }
    }
}