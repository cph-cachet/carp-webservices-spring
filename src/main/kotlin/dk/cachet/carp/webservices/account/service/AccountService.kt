package dk.cachet.carp.webservices.account.service

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role
import kotlinx.datetime.Instant

interface AccountService
{
    suspend fun invite(identity: AccountIdentity, role: Role, redirectUri: String? = null): Account
    suspend fun findByUUID(uuid: UUID): Account?
    suspend fun findByAccountIdentity(identity: AccountIdentity): Account?
    suspend fun hasRoleByEmail(email: EmailAddress, role: Role): Boolean
    suspend fun addRole(identity: AccountIdentity, role: Role)
}