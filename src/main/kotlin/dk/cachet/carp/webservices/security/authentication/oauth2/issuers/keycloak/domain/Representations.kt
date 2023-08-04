package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRepresentation(
    var id: String? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var emailVerified: Boolean? = null,
    var requiredActions: List<RequiredActions>? = null,
    var enabled: Boolean? = null
) {
    companion object {
        fun createFromAccount(account: Account): UserRepresentation {
            val userRepresentation = UserRepresentation()
            userRepresentation.id = account.id
            userRepresentation.username = account.username
            userRepresentation.firstName = account.firstName
            userRepresentation.lastName = account.lastName
            userRepresentation.email = account.email

            return userRepresentation
        }
    }

    fun setDefaultActions(): UserRepresentation {
        requiredActions = RequiredActions.getActionsForNewAccount()
        emailVerified = false
        enabled = true

        return this
    }

    fun toAccount(roles: Set<Role>): Account {
        val account = Account()
        account.id = id
        account.username = username
        account.firstName = firstName
        account.lastName = lastName
        account.email = email
        account.role = roles.max()

        return account
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RoleRepresentation(
    var id: String? = null,
    var name: String? = null
)

enum class RequiredActions {
    VERIFY_EMAIL,
    UPDATE_PROFILE,
    UPDATE_PASSWORD,
    CONFIGURE_TOTP,
    TERMS_AND_CONDITIONS;

    companion object {
        fun getActionsForNewAccount(): List<RequiredActions> =
            listOf(
                VERIFY_EMAIL,
                UPDATE_PASSWORD,
                UPDATE_PROFILE
            )

        fun getActionsForExistingAccount(): List<RequiredActions> =
            listOf(
                UPDATE_PASSWORD,
                UPDATE_PROFILE
           )
    }
}

