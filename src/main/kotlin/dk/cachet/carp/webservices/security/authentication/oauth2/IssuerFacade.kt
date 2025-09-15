package dk.cachet.carp.webservices.security.authentication.oauth2

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.RequiredActions
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role

@Suppress("TooManyFunctions")
interface IssuerFacade {
    suspend fun createAccount(account: Account): Account

    suspend fun getAccount(uuid: UUID): Account?

    suspend fun getAccount(identity: AccountIdentity): Account?

    suspend fun getAllByClaim(claim: Claim): List<Account>

    suspend fun updateAccount(account: Account): Account

    suspend fun deleteAccount(id: String)

    suspend fun addRole(
        account: Account,
        role: Role,
    )

    suspend fun getRoles(id: UUID): Set<Role>

    suspend fun executeActions(
        account: Account,
        redirectUri: String?,
        actions: List<RequiredActions>,
    )

    suspend fun recoverAccount(
        account: Account,
        clientId: String,
        redirectUri: String?,
        expirationSeconds: Long?,
    ): String

    suspend fun getRedirectUrisForClient(): List<String>
}
