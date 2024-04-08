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

        return isResearcherPartOfTheDeployment(deploymentId) || isParticipantPartOfTheDeployment(deploymentId)
    }

    fun canViewDeployment(deploymentId: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return isResearcherPartOfTheDeployment(deploymentId) || isParticipantPartOfTheDeployment(deploymentId)
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

    /**
     * Statistics endpoint is disabled, due to a refactor of the authorization
     * services with clear service boundaries. Also, none of the current clients
     * rely on this functionality.
     *
     * If there is ever a need for a statistics endpoint, there should probably be
     * at least two of those: one for study management, that takes in a study ID and
     * calculates all the relevant statistics for a study, and one which takes a single
     * deployment ID as parameter, this could be used for displaying study related
     * statistics for a single participant group.
     */
    fun canGetStatistics(deploymentIds: List<String>): Boolean = false

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

    private fun canResearcherAccessDeployments(ids: Set<UUID>): Boolean = runBlocking {
        val deployments = coreDeploymentRepository.getWSStudyDeploymentsBy(ids)
        deployments.map { it.deployedFromStudyId!! }.forEach {
            if (!isResearcherPartOfTheStudy((it)))
            {
                return@runBlocking false
            }
        }
        return@runBlocking true
    }
}