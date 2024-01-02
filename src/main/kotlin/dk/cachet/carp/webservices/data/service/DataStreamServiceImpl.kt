package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.dataPoint.controller.DataPointController
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DataStreamServiceImpl(
        private val dataStreamSequenceRepository: DataStreamSequenceRepository,
        private val dataStreamIdRepository: DataStreamIdRepository
): IDataStreamService {

    override fun getDataStream(deploymentIds: List<String>): List<DataStreamSequence> {
        return dataStreamSequenceRepository.findAllByDeploymentIds(deploymentIds)
    }

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant?
    {
        val pageRequest = PageRequest.of(0, DataPointController.DEFAULT_PAGE_SIZE)
        val dataStreamInputs = dataStreamIdRepository.getAllByDeploymentId(
            deploymentId.stringRepresentation, pageRequest)
        val sortedDataPoint = dataStreamInputs.sortedByDescending {it.updatedAt}.firstOrNull()
            ?: return null

        return sortedDataPoint.updatedAt?.toKotlinInstant()
    }
}