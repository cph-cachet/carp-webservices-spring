package dk.cachet.carp.webservices.deployment.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.domain.users.ParticipationRepository
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.deployment.service.ParticipationService
import dk.cachet.carp.webservices.export.service.ResourceExporter
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class ParticipationServiceWrapper(
    private val repository: ParticipationRepository,
    services: CoreServiceContainer,
) : ParticipationService, ResourceExporter<ParticipantData> {
    final override val core = services.participationService

    // This function is used to bypass then `inDeployment` Claim check for researchers,
    // as currently we are not granting researcher access to the deployment service.
    // Due to Claims are saved in tokens, and we don't want large tokens.
    override suspend fun participationDataRequest(request: ParticipationServiceRequest<*>) {
        when (request) {
            is ParticipationServiceRequest.GetParticipantData,
            -> core.getParticipantData(request.studyDeploymentId)
            is ParticipationServiceRequest.GetParticipantDataList,
            -> core.getParticipantDataList(request.studyDeploymentIds)
            is ParticipationServiceRequest.SetParticipantData,
            -> core.setParticipantData(request.studyDeploymentId, request.data)
            is ParticipationServiceRequest.GetActiveParticipationInvitations,
            -> Unit
        }
    }

    final override val dataFileName = "participant-data.json"

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ) = deploymentIds.map {
        val group = repository.getParticipantGroupOrThrowBy(it)
        ParticipantData(group.studyDeploymentId, group.commonData, group.roleData)
    }
}
