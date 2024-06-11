package dk.cachet.carp.webservices.data.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest

data class DataStreamServiceRequestDTO (

    val apiVersion: ApiVersion,
    val requestType: String,
    val studyDeploymentId: UUID? = null,
    val batch: DataStreamBatch? = null,
    val dataStream: DataStreamId? = null,
    val fromSequenceId: Long? = null,
    val toSequenceIdInclusive: Long? = null,
    val studyDeploymentIds: Set<UUID>? = null
) {
    fun toDataStreamServiceRequest(): DataStreamServiceRequest<*> {
        return when (requestType) {
            "AppendToDataStreams" ->
                DataStreamServiceRequest.AppendToDataStreams(studyDeploymentId!!, batch!!)
            else -> throw IllegalArgumentException("Unsupported request type: $requestType")
        }
    }
}
