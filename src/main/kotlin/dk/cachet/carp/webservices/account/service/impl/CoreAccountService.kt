package dk.cachet.carp.webservices.account.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.AnyDeviceConfiguration
import dk.cachet.carp.common.application.devices.CustomProtocolDevice
import dk.cachet.carp.common.application.tasks.CustomProtocolTask
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.webservices.common.email.domain.EmailType
import dk.cachet.carp.webservices.common.email.service.EmailInvitationService
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import dk.cachet.carp.common.domain.users.Account as CoreAccount

@Service
class CoreAccountService(
    private val accountService: dk.cachet.carp.webservices.account.service.AccountService,
    private val deploymentRepository: CoreDeploymentRepository,
    private val emailInvitationService: EmailInvitationService,
) : AccountService {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val REDIRECT_URL_KEY = "redirectURL"
    }

    override suspend fun findAccount(identity: AccountIdentity): CoreAccount? {
        val account: Account? = accountService.findByAccountIdentity(identity)

        if (account == null) {
            LOGGER.info("User not found for account identity: $identity")
            return null
        }

        return CoreAccount(identity, UUID(account.id!!))
    }

    override suspend fun inviteExistingAccount(
        accountId: UUID,
        invitation: StudyInvitation,
        participation: Participation,
        devices: List<AnyDeviceConfiguration>
    ) {
        val deployment = deploymentRepository.getStudyDeploymentBy(participation.studyDeploymentId)
        requireNotNull(deployment)

        val account = accountService.findByUUID(accountId)
        if ( account == null ) {
            LOGGER.error("Account not found for id: $accountId")
            return
        }

        inviteWithIdentity(account.getIdentity(), invitation, participation, deployment)
    }

    override suspend fun inviteNewAccount(
        identity: AccountIdentity,
        invitation: StudyInvitation,
        participation: Participation,
        devices: List<AnyDeviceConfiguration>
    ): CoreAccount {
        val deployment = deploymentRepository.getStudyDeploymentBy(participation.studyDeploymentId)
        requireNotNull(deployment)

        val account = inviteWithIdentity(identity, invitation, participation, deployment)

        return CoreAccount(identity, UUID(account.id!!))
    }

    private suspend fun inviteWithIdentity(
        accountIdentity: AccountIdentity,
        invitation: StudyInvitation,
        participation: Participation,
        deployment: StudyDeployment
    ): Account {
        if (accountIdentity is EmailAccountIdentity) {
            emailInvitationService.inviteToStudy(
                accountIdentity.emailAddress.address,
                participation.studyDeploymentId,
                invitation,
                EmailType.INVITE_EXISTING_ACCOUNT
            )
        }

        return accountService.invite(
            accountIdentity,
            Role.PARTICIPANT,
            getRedirectUrl(deployment.protocol)
        )
    }

    /**
     * Determine the redirect URL based on the study protocol which is being deployed.
     *
     * TODO: figure out a way to get and store redirect uris other than relying on the protocol
     */
    private fun getRedirectUrl(protocol: StudyProtocol): String? {
        // If it is a custom protocol study, try to extract the `redirectUrl` from the custom defined protocol.
        val customProtocolDevice = protocol
            .primaryDevices.singleOrNull() as? CustomProtocolDevice
        val customProtocol = (protocol.tasks.singleOrNull() as? CustomProtocolTask)?.studyProtocol
        if (customProtocolDevice != null && customProtocol != null) {
            try {
                val redirectUrl = (Json.parseToJsonElement(customProtocol) as? JsonObject)
                    ?.get(REDIRECT_URL_KEY) as? JsonPrimitive
                return redirectUrl?.content
            } catch (_: SerializationException) {
                // If the customProtocol is not JSON, it cannot contain `redirectUrl`, and it can thus be ignored.
            }
        }

        // No known URL to redirect to could be inferred.
        return null
    }
}