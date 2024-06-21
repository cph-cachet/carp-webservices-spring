package dk.cachet.carp.webservices.data.domain

import dk.cachet.carp.common.application.UUID
import kotlinx.serialization.Serializable
import org.springframework.web.multipart.MultipartFile

@Serializable
data class DataStreamRequestZip(
    val studyDeploymentId: UUID,
    val zipFile: MultipartFile
)
