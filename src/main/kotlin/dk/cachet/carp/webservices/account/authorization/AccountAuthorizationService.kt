package dk.cachet.carp.webservices.account.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.account.domain.AccountRequest
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class AccountAuthorizationService(
    studyService: CoreStudyRepository,
    deploymentRepository: CoreDeploymentRepository,
    participantGroupRepository: ParticipantGroupRepository,
    objectMapper: ObjectMapper,
    authenticationService: AuthenticationService
): AuthorizationService( studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService )
{
    fun canInvite(accountRequest: AccountRequest): Boolean = canAccountRequestInfoOfAnother( accountRequest )
    fun canQueryRole(accountRequest: AccountRequest) : Boolean = canAccountRequestInfoOfAnother( accountRequest )

    private fun canAccountRequestInfoOfAnother(accountRequest: AccountRequest) : Boolean
    {
        val requesterRole = authenticationService.getCurrentPrincipal().role!!

        return requesterRole >= Role.RESEARCHER && requesterRole >= accountRequest.role
    }
}