package dk.cachet.carp.webservices.account.service.impl

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authorization.Role
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service

@Service
class AccountServiceImpl(
    private val issuerFacade: IssuerFacade,
    private val accountFactory: AccountFactory
) : AccountService {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun invite(identity: AccountIdentity, role: Role, redirectUri: String?): Account {
        var isNewAccount = false
        var account = findByAccountIdentity(identity)

        if (account == null) {
            isNewAccount = true
            issuerFacade.createAccount(accountFactory.fromAccountIdentity(identity))
            LOGGER.info("User created for account identity: $identity")
            account = findByAccountIdentity(identity)
        }

        requireNotNull(account)

        LOGGER.info("Adding role: $role for user: $identity")
        issuerFacade.addRole(account, role)
        account.role = role

        LOGGER.info("Sending invitation to user: $identity")
        issuerFacade.sendInvitation(account, redirectUri, isNewAccount)

        return account
    }

    override suspend fun findByUUID(uuid: UUID): Account? =
        try {
            issuerFacade.getAccount(uuid)
        } catch (e: Exception) {
            null
        }

    override suspend fun findByAccountIdentity(identity: AccountIdentity): Account? =
        try {
            issuerFacade.getAccount(identity)
        } catch (e: Exception) {
            null
        }

    override suspend fun hasRoleByEmail(email: EmailAddress, role: Role): Boolean {
        val account = findByAccountIdentity(AccountIdentity.fromEmailAddress(email.address))

        requireNotNull(account)

        return account.role!! >= role
    }

    override suspend fun addRole(identity: AccountIdentity, role: Role) {
        val account = findByAccountIdentity(identity)

        requireNotNull(account)

        LOGGER.info("Adding role: $role for user: $identity")
        issuerFacade.addRole(account, role)
    }
}