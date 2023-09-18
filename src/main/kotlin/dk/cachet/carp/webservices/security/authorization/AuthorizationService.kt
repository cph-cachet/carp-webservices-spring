package dk.cachet.carp.webservices.security.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.domain.users.AccountParticipation
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.webservices.common.exception.responses.UnauthorizedException
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.study.domain.Study
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import kotlinx.coroutines.runBlocking

open class AuthorizationService(
    // TODO: authorization service shouldn't access the repositories directly
    private val studyRepository: CoreStudyRepository,
    private val deploymentRepository: CoreDeploymentRepository,
    private val participantGroupRepository: ParticipantGroupRepository,
    private val objectMapper: ObjectMapper,
    protected val authenticationService: AuthenticationService
)
{
    fun getAccountId(): String
    {
        val accountId = authenticationService.getCurrentPrincipal().id
        requireNotNull(accountId)
        return accountId
    }

    fun isAccountSystemAdmin(): Boolean
    {
        return authenticationService.getCurrentPrincipal().role!! >= Role.SYSTEM_ADMIN
    }

    fun isAccountCarpAdmin(): Boolean
    {
        return authenticationService.getCurrentPrincipal().role!! >= Role.CARP_ADMIN
    }

    fun isAccountResearcher(): Boolean
    {
        return authenticationService.getCurrentPrincipal().role!! >= Role.RESEARCHER
    }

    fun isResearcherPartOfTheDeployment(deploymentId: String, accountId: String): Boolean = runBlocking {
        if (!isAccountResearcher()) return@runBlocking false

        val deployment = deploymentRepository.getWSDeploymentById(UUID(deploymentId))?: return@runBlocking false
        return@runBlocking isResearcherPartOfTheStudy(deployment.deployedFromStudyId!!, accountId)
    }

    fun isParticipantPartOfTheDeployment(deploymentId: String, accountId: String): Boolean
    {
        val participantGroup = participantGroupRepository.findByStudyDeploymentId(deploymentId)
        if (!participantGroup.isPresent) throw UnauthorizedException("Could not find participant group with deploymentId: $deploymentId")
        val accountIds = participantGroup.get().snapshot!!.get("participations")
                .toSet()
                .map { it.get("accountId").textValue() }

        return accountIds.contains(getAccountId())
    }

    fun isParticipantOrResearcherOfTheStudy(studyId: String): Boolean
    {
        val accountId = getAccountId()

        return isParticipantPartOfStudy(studyId) || isResearcherPartOfTheStudy(studyId, accountId)
    }

    fun isParticipantPartOfStudy(studyId: String): Boolean
    {
        return isParticipantAccountPartOfTheStudy(authenticationService.getCurrentPrincipal(), studyId)
    }

    fun isResearcherPartOfTheStudy(studyId: String, accountId: String): Boolean = runBlocking {
        if (!isAccountResearcher()) return@runBlocking false

        val study = studyRepository.getWSStudyById(UUID(studyId))
        return@runBlocking isAccountOwnerOfTheStudy(accountId, study) || isAccountResearcherInTheStudy(accountId, study)
    }

    private fun isParticipantAccountPartOfTheStudy(account: Account, studyId: String): Boolean = runBlocking {
        val studyDeployments = deploymentRepository.getDeploymentSnapshotsByStudyId(studyId).map { it.id.stringRepresentation }
        val participantGroupSnapshotList = participantGroupRepository.findAllByStudyDeploymentIds(studyDeployments).map { objectMapper.treeToValue(it.snapshot, ParticipantGroupSnapshot::class.java) }
        val participantGroups = participantGroupSnapshotList.filter { studyDeployments.contains(it.studyDeploymentId.stringRepresentation) }
        val accountParticipationList: ArrayList<AccountParticipation> = arrayListOf()
        participantGroups.map { accountParticipationList.addAll(it.participations) }
        val accounts = accountParticipationList.map {it.accountId.stringRepresentation}
        return@runBlocking accounts.contains(account.id)
    }

    private fun isAccountOwnerOfTheStudy(accountId: String, study: Study): Boolean
    {
        val studyOwner = study.snapshot!!.get("ownerId").textValue()
        return accountId == studyOwner
    }

    private fun isAccountResearcherInTheStudy(accountId: String, study: Study): Boolean
    {
        return study.researcherAccountIds.contains(accountId)
    }
}