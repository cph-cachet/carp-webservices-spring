package dk.cachet.carp.webservices.security.authentication.oauth2

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role

interface IssuerFacade {
    suspend fun createAccount(account: Account)
    suspend fun getAccount(uuid: UUID): Account?
    suspend fun getAccount(identity: AccountIdentity): Account?
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: String)
    suspend fun addRole(account: Account, role: Role)
    suspend fun getRoles(id: UUID): Set<Role>
    suspend fun sendInvitation(account: Account, redirectUri: String?, isNewAccount: Boolean)
}