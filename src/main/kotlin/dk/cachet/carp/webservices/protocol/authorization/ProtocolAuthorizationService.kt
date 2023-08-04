package dk.cachet.carp.webservices.protocol.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHost
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot

import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class ProtocolAuthorizationService(
        coreProtocolRepository: CoreProtocolRepository,
        studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    private val protocolService: ProtocolService = ProtocolServiceHost(coreProtocolRepository)

    fun canCreateProtocol(): Boolean
    {
        return isAccountResearcher()
    }

    fun canAddVersion(protocol: StudyProtocolSnapshot): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return protocol.ownerId.stringRepresentation == authenticationService.getCurrentPrincipal().id
    }

    fun canGetAllForAnOwner(): Boolean
    {
        return canViewProtocol()
    }

    fun canGetVersionHistoryForAProtocol(): Boolean
    {
        return canViewProtocol()
    }

    fun canViewProtocol(): Boolean
    {
        return isAccountResearcher()
    }

    fun canCreateCustomProtocol(): Boolean
    {
        return canViewProtocol()
    }

    suspend fun canUpdateParticipantDataConfiguration(id: UUID, versionTag: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return isUserOwnerOfTheProtocol(id, versionTag)
    }

    private suspend fun isUserOwnerOfTheProtocol(id: UUID, versionTag: String): Boolean  {
        val protocol = protocolService.getBy(id, versionTag)

        return protocol.ownerId.stringRepresentation == authenticationService.getCurrentPrincipal().id
    }
}