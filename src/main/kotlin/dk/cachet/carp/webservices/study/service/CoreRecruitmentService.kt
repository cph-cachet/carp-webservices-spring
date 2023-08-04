package dk.cachet.carp.webservices.study.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHost
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.study.domain.ParticipantAccount
import dk.cachet.carp.webservices.study.domain.ParticipantGroupInfo
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import org.springframework.stereotype.Component

@Component
class CoreRecruitmentService
(
    participantRepository: CoreParticipantRepository,
    coreDeploymentService: CoreDeploymentService,
    coreEventBus: CoreEventBus,
    private val accountService: AccountService,
    private val accountFactory: AccountFactory
)
{
    final val instance: RecruitmentService = RecruitmentServiceHost(
            participantRepository,
            coreDeploymentService.instance,
            coreEventBus.createApplicationServiceAdapter(RecruitmentService::class)
    )

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
            participantGroupStatus.participants.map {
                val participantAccount = ParticipantAccount.fromParticipant(it)
                val account = accountService.findByAccountIdentity(it.accountIdentity)
                if (account != null) {
                    participantAccount.lateInitFrom(account)
                }
                participantAccounts.add(participantAccount)
            }

            val info = ParticipantGroupInfo(participantGroupStatus.id, participantAccounts)
            participantGroupInfoList.add(info)
        }
        return ParticipantGroupsStatus(participantGroupInfoList, participantGroupStatusList)
    }
}