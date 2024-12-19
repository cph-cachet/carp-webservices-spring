package dk.cachet.carp.webservices.datastream.service

import dk.cachet.carp.webservices.datastream.repository.DataStreamIdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchValidatedDataStreamId(
    dataStream: dk.cachet.carp.data.application.DataStreamId,
    dataStreamIdRepository: DataStreamIdRepository,
): dk.cachet.carp.webservices.datastream.domain.DataStreamId {
    return withContext(Dispatchers.IO) {
        dataStreamIdRepository.findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
            studyDeploymentId = dataStream.studyDeploymentId.stringRepresentation,
            deviceRoleName = dataStream.deviceRoleName,
            name = dataStream.dataType.name,
            nameSpace = dataStream.dataType.namespace,
        )
    }.orElseThrow {
        IllegalArgumentException("Data stream ID not found for the specified parameters.")
    }
}
