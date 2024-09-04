package dk.cachet.carp.webservices.study.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.config.SecurityCoroutineContext
import dk.cachet.carp.webservices.study.domain.InactiveDeploymentInfo
import dk.cachet.carp.webservices.study.domain.ParticipantAccount
import dk.cachet.carp.webservices.study.domain.ParticipantGroupInfo
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.exceptions.StudyServiceException
import dk.cachet.carp.webservices.study.service.RecruitmentService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service

@Service
class RecruitmentServiceWrapper(
    private val accountService: AccountService,
    private val dataStreamService: DataStreamService,
    private val authenticationService: AuthenticationService,
    private val studyService: StudyServiceWrapper,
    services: CoreServiceContainer,
) : RecruitmentService {
    final override val core = services.recruitmentService

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun inviteResearcher(
        studyId: UUID,
        email: String,
    ) = withContext(Dispatchers.IO + SecurityCoroutineContext()) {
        val accountIdentity = AccountIdentity.fromEmailAddress(email)
        var account = accountService.findByAccountIdentity(accountIdentity)

        if (account == null) {
            LOGGER.info("Account with email $email is not found. Inviting...")
            account = accountService.invite(accountIdentity, Role.RESEARCHER)
        }

        if (account.role!! < Role.RESEARCHER) {
            accountService.addRole(accountIdentity, Role.RESEARCHER)
            LOGGER.info("Account with email $email is granted the role RESEARCHER.")
        }

        // grant it claims for the study and every deployment within it
        accountService.grant(accountIdentity, setOf(Claim.ManageStudy(studyId)))

        LOGGER.info("Account with email $email is added as a researcher to study with id $studyId.")
    }

    override suspend fun removeResearcher(
        studyId: UUID,
        email: String,
    ): Boolean =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            val accountIdentity = AccountIdentity.fromEmailAddress(email)

            val claims = setOf(Claim.ManageStudy(studyId))

            val account = accountService.revoke(accountIdentity, claims)

            account.carpClaims?.intersect(claims)?.isEmpty() ?: false
        }

    override suspend fun getParticipants(
        studyId: UUID,
        offset: Int,
        limit: Int,
    ): List<Account> =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            var participants = core.getParticipants(studyId)
            val accounts = arrayListOf<Account>()
            if (offset >= 0 && limit > 0) {
                participants = participants.drop(offset * limit).take(limit).toMutableList()
            }

            for (participant in participants) {
                val account = accountService.findByAccountIdentity(participant.accountIdentity)
                accounts.add(account ?: Account.fromAccountIdentity(participant.accountIdentity))
            }
            accounts
        }

    override suspend fun getInactiveDeployments(
        studyId: UUID,
        lastUpdate: Int,
        offset: Int,
        limit: Int,
    ): List<InactiveDeploymentInfo> {
        val timeNow: Instant = Clock.System.now()

        val participantGroupStatusList =
            core.getParticipantGroupStatusList(studyId)
                .filterIsInstance<ParticipantGroupStatus.InDeployment>()

        val inactiveDeploymentInfoList =
            participantGroupStatusList
                .map {
                    val lastDataUpload =
                        dataStreamService.getLatestUpdatedAt(
                            it.studyDeploymentStatus.studyDeploymentId,
                        )
                    InactiveDeploymentInfo(it.id, lastDataUpload)
                }
                .filter {
                    it.dateOfLastDataUpload != null &&
                        it.dateOfLastDataUpload.plus(lastUpdate, DateTimeUnit.HOUR) < timeNow
                }

        if (offset >= 0 && limit > 0) {
            return inactiveDeploymentInfoList.drop(offset * limit).take(limit).sortedBy { it.dateOfLastDataUpload }
        }

        return inactiveDeploymentInfoList.sortedBy { it.dateOfLastDataUpload }
    }

    override fun isParticipant(
        studyId: UUID,
        accountId: UUID,
    ): Boolean =
        runBlocking(SecurityCoroutineContext()) {
            getParticipants(studyId).any { it.id == accountId.toString() }
        }

    override suspend fun getParticipantGroupsStatus(studyId: UUID): ParticipantGroupsStatus =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            if (!studyService.studyExists(studyId)) {
                throw StudyServiceException.studyNotFound(studyId)
            }

            if (!authenticationService.getClaims().contains(Claim.ManageStudy(studyId))) {
                throw StudyServiceException.manageStudyPermissionDenied()
            }

            val participantGroupStatusList = core.getParticipantGroupStatusList(studyId)

            val participantGroupInfoList =
                participantGroupStatusList
                    .filterIsInstance<ParticipantGroupStatus.InDeployment>()
                    .map {
                        val participantAccounts =
                            it.participants.map { participant ->
                                val participantAccount = ParticipantAccount.fromParticipant(participant)
                                val account = accountService.findByAccountIdentity(participant.accountIdentity)

                                if (account != null) {
                                    val lastDataUpload =
                                        dataStreamService.getLatestUpdatedAt(
                                            it.studyDeploymentStatus.studyDeploymentId,
                                        )
                                    participantAccount.lateInitFrom(account)

                                    // TODO: we cannot track this for participants, only for deployments
                                    participantAccount.dateOfLastDataUpload = lastDataUpload
                                }

                                participantAccount
                            }

                        ParticipantGroupInfo(it.id, it.studyDeploymentStatus, participantAccounts)
                    }

            ParticipantGroupsStatus(participantGroupInfoList, participantGroupStatusList)
        }
}
