package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.data.service.DataStreamService
import dk.cachet.carp.webservices.export.service.ResourceExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class DataStreamServiceWrapper(
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository,
    services: CoreServiceContainer,
) : DataStreamService, ResourceExporter<DataStreamSequence<Any?>> {
    final override val core = services.dataStreamService

    override val dataFileName: String = "data-streams.json" // Implementing the required property

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant? {
        val dataStreamInputs =
            dataStreamIdRepository.getAllByDeploymentId(
                deploymentId.toString(),
            )
        val sortedDataPoint =
            dataStreamInputs.sortedByDescending { it.updatedAt }.firstOrNull()
                ?: return null

        return sortedDataPoint.updatedAt?.toKotlinInstant()
    }

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ) = withContext(Dispatchers.IO) {
        dataStreamSequenceRepository.findAllByDeploymentIds(
            deploymentIds.map { it.toString() },
        )
    }
}
