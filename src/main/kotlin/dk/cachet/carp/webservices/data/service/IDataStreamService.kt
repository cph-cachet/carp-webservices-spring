package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import kotlinx.datetime.Instant

interface IDataStreamService {
    fun getDataStream(deploymentIds: List<String>) : List<DataStreamSequence>
    fun getLatestUpdatedAt(deploymentId: UUID): Instant?
    fun fromZipToBatch(studyDeploymentId: UUID, zipFile: ByteArray): DataStreamSequence
}