package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository

class DataStreamServiceImpl(
        private val dataStreamSequenceRepository: DataStreamSequenceRepository
): IDataStreamService {

    override fun getDataStream(deploymentIds: List<String>): List<DataStreamSequence> {
        return dataStreamSequenceRepository.findAllByDeploymentIds(deploymentIds)
    }

}