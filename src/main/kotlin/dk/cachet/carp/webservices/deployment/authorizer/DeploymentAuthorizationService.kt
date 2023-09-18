package dk.cachet.carp.webservices.deployment.authorizer

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class DeploymentAuthorizationService(
        private val coreDeploymentRepository: CoreDeploymentRepository,
        studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    fun canCreateDeployment(): Boolean = isAccountResearcher()

    fun canRegisterDevice(request: DeploymentServiceRequest.RegisterDevice): Boolean
    {
        val deploymentId = request.studyDeploymentId.stringRepresentation
        return canModifyDeployment(deploymentId)
    }

    fun canGetMasterDeviceDeployments(deploymentId: String): Boolean
    {
        return canViewDeployment(deploymentId)
    }

    fun canGetParticipationInvitations(accountId: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        val account = authenticationService.getCurrentPrincipal()
        return account.id == accountId
    }

    fun canGetParticipantData(deploymentId: String): Boolean
    {
        return canViewDeployment(deploymentId)
    }

    fun canGetParticipantDataList(deploymentIds: Set<UUID>): Boolean
    {
        val ids = deploymentIds.map { id -> id.stringRepresentation }
        for (id in ids)
        {
            val allowed = canViewDeployment(id)
            if (!allowed) return false
        }
        return true
    }

    fun canSetParticipantData(deploymentId: String): Boolean
    {
        return canModifyDeployment(deploymentId)
    }

    fun canModifyDeployment(deploymentId: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        val accountId = getAccountId()

        return isResearcherPartOfTheDeployment(deploymentId, accountId) || isParticipantPartOfTheDeployment(deploymentId, accountId)
    }

    fun canViewDeployment(deploymentId: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        val accountId = getAccountId()

        return isResearcherPartOfTheDeployment(deploymentId, accountId) || isParticipantPartOfTheDeployment(deploymentId, accountId)
    }

    fun canGetDeploymentSuccessfulStatus(deploymentId: String): Boolean
    {
        return canViewDeployment(deploymentId)
    }

    fun canUnregisterDevice(deploymentId: String): Boolean
    {
        return canModifyDeployment(deploymentId)
    }

    fun canStop(deploymentId: String): Boolean
    {
        return canModifyDeployment(deploymentId)
    }

    fun canGetStudyDeploymentStatusList(request: DeploymentServiceRequest.GetStudyDeploymentStatusList): Boolean
    {
        if (isAccountSystemAdmin()) return true

        val studyDeploymentIds = request.studyDeploymentIds
        return canAccessDeployments(studyDeploymentIds)
    }

    fun canGetStatistics(deploymentIds: List<String>): Boolean {
        if (isAccountSystemAdmin()) return true
        if (!isAccountResearcher()) return false

        val accountId = getAccountId()

        val uuids = deploymentIds.map { UUID(it) }.toSet()
        return canResearcherAccessDeployments(uuids, accountId)
    }

    fun canDeleteDeployments(deploymentIds: Set<UUID>): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return canAccessDeployments(deploymentIds)
    }

    private fun canAccessDeployments(ids: Set<UUID>): Boolean
    {
        ids.map { it.stringRepresentation }.forEach {
            if (!canViewDeployment(it))
            {
                return false
            }
        }
        return true
    }

    private fun canResearcherAccessDeployments(ids: Set<UUID>, accountId: String): Boolean = runBlocking {


        val deployments = coreDeploymentRepository.getWSStudyDeploymentsBy(ids)
        deployments.map { it.deployedFromStudyId!! }.forEach {
            if (!isResearcherPartOfTheStudy((it), accountId))
            {
                return@runBlocking false
            }
        }
        return@runBlocking true
    }
}