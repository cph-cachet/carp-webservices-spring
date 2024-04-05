package dk.cachet.carp.webservices.study.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHost
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.data.service.IDataStreamService
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.study.domain.AnonymousParticipant
import dk.cachet.carp.webservices.study.domain.ParticipantAccount
import dk.cachet.carp.webservices.study.domain.ParticipantGroupInfo
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import kotlinx.datetime.Clock
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Component
class CoreRecruitmentService(
    participantRepository: CoreParticipantRepository,
    coreEventBus: CoreEventBus,
    private val coreDeploymentService: CoreDeploymentService,
    private val coreStudyService: CoreStudyService,
    private val dataStreamService: IDataStreamService,
    private val accountService: AccountService,
    private val accountFactory: AccountFactory,
) {
    final val instance: RecruitmentService = RecruitmentServiceHost(
        participantRepository,
        coreDeploymentService.instance,
        coreEventBus.createApplicationServiceAdapter(RecruitmentService::class)
    )

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    suspend fun getParticipantAccounts(studyId: UUID) : List<Account>
    {
        val participants = instance.getParticipants(studyId)
        val accounts = arrayListOf<Account>()
        for (participant in participants) {
            val account = accountService.findByAccountIdentity(participant.accountIdentity)
            if (account != null) {
                accounts.add(account)
            } else {
                accounts.add(accountFactory.fromAccountIdentity(participant.accountIdentity))
            }
        }
        return accounts
    }

    suspend fun getParticipantGroupStatus(studyId: UUID): ParticipantGroupsStatus {
        val participantGroupStatusList = instance.getParticipantGroupStatusList(studyId)
        val participantGroupInfoList = arrayListOf<ParticipantGroupInfo>()

        for (participantGroupStatus in participantGroupStatusList) {

            val participantAccounts = arrayListOf<ParticipantAccount>()
            val deploymentStatus = coreDeploymentService.instance.getStudyDeploymentStatus(participantGroupStatus.id)
            participantGroupStatus.participants.map {
                val participantAccount = ParticipantAccount.fromParticipant(it)
                val account = accountService.findByAccountIdentity(it.accountIdentity)
                if (account != null) {
                    val lastDataUpload = dataStreamService.getLatestUpdatedAt(participantGroupStatus.id)
                    participantAccount.lateInitFrom(account)
                    participantAccount.dateOfLastDataUpload = lastDataUpload
                }
                participantAccounts.add(participantAccount)
            }

            val info = ParticipantGroupInfo(participantGroupStatus.id, deploymentStatus, participantAccounts)
            participantGroupInfoList.add(info)
        }
        return ParticipantGroupsStatus(participantGroupInfoList, participantGroupStatusList)
    }

    suspend fun createAnonymousParticipants(
        studyId: UUID,
        amount: Int,
        expirationSeconds: Long,
        participantRoleName: String,
        redirectUri: String?
    ): List<AnonymousParticipant> {
        LOGGER.info("Generating $amount anonymous participants for study $studyId")

        val protocol = coreStudyService.instance.getStudyDetails(studyId).protocolSnapshot
        requireNotNull( protocol ) { "No protocol found for study $studyId." }

        require( protocol.participantRoles.any { participantRole -> participantRole.role == participantRoleName } )
        {
            "Participant role '$participantRoleName' not found in study protocol."
        }

        val participants = arrayListOf<AnonymousParticipant>()

        for (i in 0 until amount) {
            val username = UUID.randomUUID()

            val link = accountService.generateTemporaryAccount(
                UsernameAccountIdentity(username.toString()),
                expirationSeconds,
                redirectUri
            )

            val participant = instance.addParticipant(studyId, Username(username.toString()))
            val groupStatus = instance.inviteNewParticipantGroup(
                studyId,
                setOf(AssignedParticipantRoles(participant.id, AssignedTo.Roles(setOf(participantRoleName))))
            ) as ParticipantGroupStatus.InDeployment

            participants.add(
                AnonymousParticipant(
                    username,
                    groupStatus.studyDeploymentStatus.studyDeploymentId,
                    link,
                    Clock.System.now() + expirationSeconds.toDuration(DurationUnit.SECONDS)
                )
            )
        }

        return participants
    }
}