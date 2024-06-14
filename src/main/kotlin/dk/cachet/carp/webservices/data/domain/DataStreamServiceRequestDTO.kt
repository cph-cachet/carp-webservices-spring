package dk.cachet.carp.webservices.data.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DataStreamServiceRequestDTO(
    @JsonProperty
    val dType: String = "",
    @JsonProperty("apiVersion")
    val apiVersion: String = "",
    @JsonProperty("studyDeploymentId")
    val studyDeploymentId: UUID? = null,
    @JsonProperty("configuration")
    @Contextual val configuration: DataStreamsConfiguration? = null,
    @JsonProperty("batch")
    val batch: DataStreamBatch? = null,
    @JsonProperty("fromSequenceId")
    val fromSequenceId: Long? = null,
    @JsonProperty("toSequenceIdInclusive")
    val toSequenceIdInclusive: Long? = null,
    @JsonProperty("studyDeploymentIds")
    val studyDeploymentIds: Set<UUID>? = null,
    @Contextual
    @JsonProperty("dataStreamId")
    val dataStreamId: DataStreamId? = null,
) {
    fun toDataStreamServiceRequest(): DataStreamServiceRequest<*>? {
        return when {
            this.configuration != null -> {
                DataStreamServiceRequest.OpenDataStreams(this.configuration)
            }
            this.studyDeploymentId != null && this.batch != null -> {
                DataStreamServiceRequest.AppendToDataStreams(this.studyDeploymentId, this.batch)
            }
            this.dataStreamId != null && this.fromSequenceId != null -> {
                DataStreamServiceRequest.GetDataStream(
                    this.dataStreamId.toApplicationDataStreamId(),
                    this.fromSequenceId,
                    this.toSequenceIdInclusive,
                )
            }
            this.studyDeploymentIds != null -> {
                // Here we assume that if studyDeploymentIds is not null,
                // we are dealing with either CloseDataStreams or RemoveDataStreams.
                // You might need to adjust this logic based on your actual requirements.
                DataStreamServiceRequest.CloseDataStreams(this.studyDeploymentIds)
                // Or: DataStreamServiceRequest.RemoveDataStreams(this.studyDeploymentIds)
            }
            else -> null
        }
    }
}

// redo the logic fun should not be defined here
fun dk.cachet.carp.webservices.data.domain.DataStreamId.toApplicationDataStreamId():
    dk.cachet.carp.data.application.DataStreamId {
    return dk.cachet.carp.data.application.DataStreamId(
        studyDeploymentId = UUID(this.studyDeploymentId ?: ""),
        deviceRoleName = this.deviceRoleName ?: "",
        dataType = DataType(nameSpace ?: "", name ?: ""),
    )
}
