package dk.cachet.carp.webservices.data.domain

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import org.springframework.web.multipart.MultipartFile

@Serializable
data class DataStreamRequest(
    val studyDeploymentId: UUID,
    val zipFile: MultipartFile
)

/*
@Serializable
data class AppendToDataStreams(
    val studyDeploymentId: UUID,
    @Serializable( DataStreamBatchSerializer::class )
    val batch: DataStreamBatch
) : DataStreamServiceRequest<Unit>()
{
    override fun getResponseSerializer() = kotlinx.serialization.serializer<Unit>()
}*/
