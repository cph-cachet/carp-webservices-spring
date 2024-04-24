package dk.cachet.carp.webservices.security.authentication.domain

import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role

data class Account(
    var id: String? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var role: Role? = null,
    var carpClaims: Set<Claim>? = emptySet(),
) {
    companion object Factory {
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