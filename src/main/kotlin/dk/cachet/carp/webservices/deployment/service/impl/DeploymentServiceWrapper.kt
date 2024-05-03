package dk.cachet.carp.webservices.deployment.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.deployment.repository.StudyDeploymentRepository
import dk.cachet.carp.webservices.deployment.service.DeploymentService
import dk.cachet.carp.webservices.export.service.ResourceExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class DeploymentServiceWrapper(
    private val repository: StudyDeploymentRepository,
    private val objectMapper: ObjectMapper,
    services: CoreServiceContainer,
): DeploymentService, ResourceExporter<StudyDeploymentSnapshot>
{
    final override val core = services.deploymentService

    final override val dataFileName = "deployments.json"

    override suspend fun exportDataOrThrow( studyId: UUID, deploymentIds: Set<UUID>, target: Path ) =
        withContext( Dispatchers.IO ) {
            repository
                .findAllByStudyDeploymentIds( deploymentIds.map { it.stringRepresentation } )
                .map { objectMapper.treeToValue( it.snapshot, StudyDeploymentSnapshot::class.java ) }
        }
}