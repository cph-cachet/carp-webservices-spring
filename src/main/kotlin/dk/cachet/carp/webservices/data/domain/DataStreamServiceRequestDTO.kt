package dk.cachet.carp.webservices.data.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import kotlinx.serialization.Contextual
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class DataStreamServiceRequestDTO (

    @Required
    val apiVersion: ApiVersion = DataStreamService.API_VERSION,
    val studyDeploymentId: UUID? = null,
    val batch: DataStreamBatch? = null,
    @Contextual val dataStreamId: DataStreamId? = null,
    val fromSequenceId: Long? = null,
    val toSequenceIdInclusive: Long? = null,
    val studyDeploymentIds: Set<UUID>? = null
) {
    fun toDataStreamServiceRequest(): DataStreamServiceRequest<*> {
        val requestType = apiVersion.toString()
        return when (requestType) {
            "AppendToDataStreams" ->
                DataStreamServiceRequest.AppendToDataStreams(studyDeploymentId!!, batch!!)
            "GetDataStream" ->
                DataStreamServiceRequest.GetDataStream(dataStreamId!!)
            else -> throw IllegalArgumentException("Unsupported request type: $requestType")
        }
    }
}
