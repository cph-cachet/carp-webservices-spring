package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserRepresentation(
    var id: String? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var emailVerified: Boolean? = false,
    var requiredActions: List<RequiredActions>? = emptyList(),
    var enabled: Boolean? = true,
    var attributes: Map<String, Any>? = emptyMap()
) {
    companion object {
        fun createFromAccount(account: Account, accountType: AccountType) =
            UserRepresentation(
                id = account.id,
                username = account.username,
                firstName = account.firstName,
                lastName = account.lastName,
                email = account.email,
                requiredActions = RequiredActions.getForAccountType(accountType),
                emailVerified = accountType == AccountType.GENERATED,
                attributes = account.carpClaims?.associate { it.toTokenClaimObject() }
            )
    }

    fun toAccount(roles: Set<Role>) =
        Account(
            id = id,
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            role = roles.max(),
            carpClaims = attributes?.mapNotNull { Claim.fromTokenClaimObject(it.key to it.value) }
        )

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
        fun getForAccountType(accountType: AccountType): List<RequiredActions> {
            return when (accountType) {
                AccountType.NEW -> listOf(
                    VERIFY_EMAIL,
                    UPDATE_PASSWORD,
                    UPDATE_PROFILE
                )

                AccountType.EXISTING -> listOf(
                    UPDATE_PASSWORD,
                    UPDATE_PROFILE
                )

                AccountType.GENERATED -> emptyList()
            }
        }
    }
}

enum class AccountType {
    NEW,
    EXISTING,
    GENERATED
}
