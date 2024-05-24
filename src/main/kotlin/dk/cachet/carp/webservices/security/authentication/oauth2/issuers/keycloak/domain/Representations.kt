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
    var requiredActions: List<RequiredAction>? = emptyList(),
    var enabled: Boolean? = true,
    var attributes: Map<String, Any>? = emptyMap()
) {
    companion object {
        fun createFromAccount(account: Account, requiredActions: List<RequiredAction> = emptyList()) =
            UserRepresentation(
                id = account.id,
                username = account.username ?: account.email,
                firstName = account.firstName,
                lastName = account.lastName,
                email = account.email,
                requiredActions = requiredActions,
                emailVerified = !requiredActions.contains( RequiredAction.VERIFY_EMAIL ),
                attributes = account.carpClaims?.groupBy( { Claim.userAttributeName( it::class ) }, { it.value } )
            )
    }

    fun toAccount( roles: Set<Role> ) =
        Account(
            id = id,
            username = username,
            firstName = firstName,
            lastName = lastName,
            email = email,
            role = roles.max(),
            carpClaims = attributes?.mapNotNull { Claim.fromUserAttribute(it.key to it.value) }?.flatten()?.toSet()
        )

}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RoleRepresentation(
    var id: String? = null,
    var name: String? = null
)

enum class RequiredAction {
    VERIFY_EMAIL,
    UPDATE_PROFILE,
    UPDATE_PASSWORD,
    CONFIGURE_TOTP,
    TERMS_AND_CONDITIONS;

    companion object {
        fun getForAccountType(accountType: AccountType): List<RequiredAction> {
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupRepresentation(

    var name: String? = null,
    var id: String? = null,
) {
    companion object {
        fun createFromStudyId(studyId: String) =
            GroupRepresentation(
                name = studyId,
            )
    }
}
