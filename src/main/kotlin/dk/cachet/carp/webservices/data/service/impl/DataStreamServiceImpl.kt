package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.data.authorization.DataStreamServiceAuthorizer
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.service.DataStreamService
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.springframework.stereotype.Service

@Service
class DataStreamServiceImpl(
    private val dataStreamIdRepository: DataStreamIdRepository,
    services: CoreServiceContainer
): DataStreamService
{
    final override val core = services.dataStreamService

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant?
    {
        val dataStreamInputs = dataStreamIdRepository.getAllByDeploymentId(
            deploymentId.toString())
        val sortedDataPoint = dataStreamInputs.sortedByDescending {it.updatedAt}.firstOrNull()
            ?: return null

        return sortedDataPoint.updatedAt?.toKotlinInstant()
    }
}