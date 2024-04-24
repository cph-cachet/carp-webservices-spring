package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.data.authorization.DataStreamServiceAuthorizer
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.data.service.DataStreamService
import dk.cachet.carp.webservices.data.service.core.CoreDataStreamService
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.springframework.stereotype.Service

@Service
class DataStreamServiceImpl(
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamServiceAuthorizer: DataStreamServiceAuthorizer,
    coreDataStreamService: dk.cachet.carp.data.application.DataStreamService
): DataStreamService {

    final override val core: DataStreamServiceDecorator

    init
    {
        val authorizedService = DataStreamServiceDecorator( coreDataStreamService )
        {
            command -> ApplicationServiceRequestAuthorizer( dataStreamServiceAuthorizer, command )
        }

        core = authorizedService
    }

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant?
    {
        val dataStreamInputs = dataStreamIdRepository.getAllByDeploymentId(
            deploymentId.toString())
        val sortedDataPoint = dataStreamInputs.sortedByDescending {it.updatedAt}.firstOrNull()
            ?: return null

        return sortedDataPoint.updatedAt?.toKotlinInstant()
    }
}