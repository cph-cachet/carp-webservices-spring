package dk.cachet.carp.webservices.account.service.impl

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.account.domain.MagicLink
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.KeycloakFacade
import dk.cachet.carp.webservices.security.authorization.Role
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class AccountServiceImpl(
    private val issuerFacade: IssuerFacade,
    private val accountFactory: AccountFactory,
    private val keycloakFacade: KeycloakFacade
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

    override suspend fun sendMagicLinks(studyId: UUID, numberOfAccounts: Number, expiryDate: Instant?) {

        // Example data, replace this with your actual data
        val csvDataList = listOf(
            MagicLink("https://example.com/magiclink1", UUID("8076f1cd-ce2a-4f98-bbdc-619de87e9f07")
                ,"94b89928-e8c2-4ece-96d6-7a03bd2e5f71", Clock.System.now()),
            MagicLink("https://example.com/magiclink2", UUID("8076f1cd-ce2a-4f98-bbdc-619de87e9f07")
                ,"94b89928-e8c2-4ece-96d6-7a03bd2e5f71", Clock.System.now())
        )

        // Output CSV file path with study ID, /w time of generation
        val nowTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val nowTimeFormatted = nowTime.format(formatter)

        val csvFile = "\"magic_links.${studyId.toString().take(8)}.$nowTimeFormatted.csv\""/*        val link = keycloakFacade.generateMagicLink(studyId)*/

        val magicLink = keycloakFacade.generateMagicLink(studyId)
        print("check")

        // Writing CSV file
        CsvWriter().open(csvFile) {
            writeRow(listOf("Magic Link", "Account ID", "Study Deployment ID", "Expiry Date"))

            csvDataList.forEach { data ->
                writeRow(listOf(
                    data.magicLink,
                    data.accountId,
                    data.studyDeploymentId,
                    data.expiryDate
                ))
            }}

    }

}