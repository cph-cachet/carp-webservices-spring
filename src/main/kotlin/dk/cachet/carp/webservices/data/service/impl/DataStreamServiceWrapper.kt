package dk.cachet.carp.webservices.data.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.data.service.DataStreamService
import dk.cachet.carp.webservices.export.service.ResourceExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Service
class DataStreamServiceWrapper(
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository,
    services: CoreServiceContainer,
) : DataStreamService, ResourceExporter<DataStreamSequence> {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    final override val core = services.dataStreamService

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant? {
        val dataStreamInputs =
            dataStreamIdRepository.getAllByDeploymentId(
                deploymentId.toString(),
            )
        val sortedDataPoint =
            dataStreamInputs.sortedByDescending { it.updatedAt }.firstOrNull()
                ?: return null

        return sortedDataPoint.updatedAt?.toKotlinInstant()
    }

    suspend fun extractFilesFromZip(zipFile: ByteArray): DataStreamServiceRequest<*>? =
        withContext(Dispatchers.IO) {
            val objectMapper = ObjectMapper()
            var sequenceId = 1L
            val dataStreamServiceRequest: DataStreamServiceRequest<*>? = null

            try {
                ZipInputStream(ByteArrayInputStream(zipFile)).use { zipInputStream ->
                    var entry: ZipEntry? = zipInputStream.nextEntry

                    while (entry != null) {
                        if (!entry.isDirectory) {
                            ByteArrayOutputStream().use { byteArrayOutputStream ->
                                val buffer = ByteArray(1024)
                                var len: Int
                                while (zipInputStream.read(buffer).also { len = it } > 0) {
                                    byteArrayOutputStream.write(buffer, 0, len)
                                }
                                val content = byteArrayOutputStream.toByteArray()
                                val snapshot: JsonNode = objectMapper.readTree(content)

                                JSON.decodeFromString(DataStreamServiceRequest.Serializer, snapshot.toString())

                                sequenceId++
                            }
                        }
                        entry = zipInputStream.nextEntry
                    }
                }
            } catch (e: IOException) {
                LOGGER.error("Error extracting files from zip", e)
                throw e
            }

            return@withContext dataStreamServiceRequest
        }

    override suspend fun processZipToInvoke(zipFile: MultipartFile): Any {
        val zipFileBytes = zipFile.bytes

        // Extract the DataStreamServiceRequest from the zip file
        val dataStreamServiceRequest =
            extractFilesFromZip(zipFileBytes)
                ?: throw IllegalArgumentException("Invalid zip file content")

        // Check if the dataStreamServiceRequest is of the correct type
        return when (dataStreamServiceRequest) {
            is DataStreamServiceRequest.AppendToDataStreams -> {
                // Invoke the existing service method
                core.invoke(dataStreamServiceRequest)
            }
            else -> {
                // Handle the case where the dataStreamServiceRequest is not of the correct type
                throw IllegalArgumentException("Invalid request type")
            }
        }
    }

    override val dataFileName = "data-streams.json"

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ) = withContext(Dispatchers.IO) {
        dataStreamSequenceRepository.findAllByDeploymentIds(
            deploymentIds.map { it.toString() },
        )
    }
}
