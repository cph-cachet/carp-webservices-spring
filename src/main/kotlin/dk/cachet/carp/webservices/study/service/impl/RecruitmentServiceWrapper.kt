package dk.cachet.carp.webservices.study.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.config.SecurityCoroutineContext
import dk.cachet.carp.webservices.study.domain.*
import dk.cachet.carp.webservices.study.repository.RecruitmentRepository
import dk.cachet.carp.webservices.study.service.RecruitmentService
import kotlinx.coroutines.*
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
    private val recruitmentRepository: RecruitmentRepository,
    private val objectMapper: ObjectMapper,
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

    override suspend fun inviteResearcherAssistant(
        studyId: UUID,
        email: String,
    ) = withContext(Dispatchers.IO + SecurityCoroutineContext()) {
        val accountIdentity = AccountIdentity.fromEmailAddress(email)
        var account = accountService.findByAccountIdentity(accountIdentity)

        if (account == null) {
            LOGGER.info("Account with email $email is not found. Inviting...")
            account = accountService.invite(accountIdentity, Role.RESEARCHER_ASSISTANT)
        }

        if (account.role!! < Role.RESEARCHER_ASSISTANT) {
            accountService.addRole(accountIdentity, Role.RESEARCHER_ASSISTANT)
            LOGGER.info("Account with email $email is granted the role RESEARCHER_ASSISTANT.")
        }

        // grant it claims for the study and every deployment within it
        accountService.grant(accountIdentity, setOf(Claim.LimitedManageStudy(studyId)))

        LOGGER.info("Account with email $email is added as a researcher assistant to study with id $studyId.")
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

    override suspend fun removeResearcherAssistant(
        studyId: UUID,
        email: String,
    ): Boolean =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            val accountIdentity = AccountIdentity.fromEmailAddress(email)

            val claims = setOf(Claim.LimitedManageStudy(studyId))

            val account = accountService.revoke(accountIdentity, claims)

            account.carpClaims?.intersect(claims)?.isEmpty() ?: false
        }

    override suspend fun getParticipants(
        studyId: UUID,
        offset: Int?,
        limit: Int?,
        search: String?,
        isDescending: Boolean?,
    ): List<Account> =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            val serializedParticipants =
                recruitmentRepository.findRecruitmentParticipantsByStudyIdAndSearchAndLimitAndOffset(
                    studyId.stringRepresentation,
                    offset,
                    limit,
                    search,
                    isDescending,
                )

            if (serializedParticipants.isNullOrEmpty()) return@withContext emptyList()

            val participants =
                objectMapper.readValue(
                    serializedParticipants,
                    object : TypeReference<List<Participant>>() {},
                )

            val accounts = arrayListOf<Account>()
            for (participant in participants) {
                val account = accountService.findByAccountIdentity(participant.accountIdentity)
                accounts.add(account ?: Account.fromAccountIdentity(participant.accountIdentity))
            }

            accounts
        }

    override suspend fun countParticipants(
        studyId: UUID,
        search: String?,
    ): Int =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            val count =
                recruitmentRepository.countRecruitmentParticipantsByStudyIdAndSearch(
                    studyId.stringRepresentation,
                    search,
                )

            count
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
            getParticipants(studyId, null, null, null, false).any { it.id == accountId.toString() }
        }

    override suspend fun getParticipantGroupsStatus(studyId: UUID): ParticipantGroupsStatus =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
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
