package dk.cachet.carp.webservices.datastream.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.domain.DataStreamSequence
import dk.cachet.carp.webservices.datastream.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.export.service.ResourceExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Service
class DataStreamService(
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository,
    services: CoreServiceContainer,
) : DataStreamService, ResourceExporter<DataStreamSequence> {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    final override val core = services.dataStreamService

    /**
     * Retrieves the latest update timestamp for a given deployment.
     *
     * @param deploymentId The ID of the deployment for which to retrieve the latest update timestamp.
     * @return The latest update timestamp as an `Instant`,
     * or null if no data stream inputs are found for the given deployment ID.
     */

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant? {
        val dataStreamIds =
            dataStreamIdRepository.getAllByDeploymentId(
                deploymentId.toString(),
            )

        val dataStreams =
            dataStreamSequenceRepository.findAllByDataStreamIds(
                dataStreamIds.map { it.id },
            )

        val sortedDataPoint =
            dataStreams.sortedByDescending { it.updatedAt }.firstOrNull()
                ?: return null

        return sortedDataPoint.updatedAt
    }

    /**
     * Processes a zip file and invokes the appropriate service method.
     * Converts the zip file to a byte array, extracts a `DataStreamServiceRequest`, and processes the request.
     * Throws an `IllegalArgumentException` if the extraction fails.
     *
     * @param zipFile The zip file to be processed.
     * @return The result of invoking the service method.
     * @throws IllegalArgumentException If the zip file content is invalid.
     */

    @Deprecated("Use decompressGzip")
    override suspend fun processZipToInvoke(zipFile: MultipartFile): Any {
        val zipFileBytes = zipFile.bytes

        val dataStreamServiceRequest =
            extractFilesFromZip(zipFileBytes)
                ?: throw IllegalArgumentException("Invalid zip file content")

        return when (dataStreamServiceRequest) {
            is DataStreamServiceRequest.AppendToDataStreams -> {
                core.invoke(dataStreamServiceRequest)
            }
            else -> {
                throw IllegalArgumentException("Invalid request type")
            }
        }
    }

    /**
     * Extracts a `DataStreamServiceRequest` from a zipped file.
     * Writes the input ByteArray to a temporary file, opens the
     * zip file, reads the content, and decodes it into a `DataStreamServiceRequest`.
     * Deletes the temporary file after extraction.
     * Operates in the IO dispatcher context for optimized I/O operations.
     *
     * @param zipFile The ByteArray representing the zipped file.
     * @return The `DataStreamServiceRequest` extracted from the
     * zipped file, or null if no request could be extracted.
     * @throws IOException If an error occurs during file operations.
     */

    @Deprecated("Use decompressGzip")
    suspend fun extractFilesFromZip(zipFile: ByteArray): DataStreamServiceRequest<*>? =
        withContext(Dispatchers.IO) {
            val objectMapper = ObjectMapper()
            var dataStreamServiceRequest: DataStreamServiceRequest<*>? = null

            try {
                val tempFile = Files.createTempFile(null, null)
                Files.write(tempFile, zipFile, StandardOpenOption.WRITE)

                Files.newInputStream(tempFile).use { inputStream ->
                    ZipArchiveInputStream(inputStream).use { zipInputStream ->
                        var zipEntry = zipInputStream.nextEntry
                        while (zipEntry != null) {
                            val content = zipInputStream.readBytes()
                            val snapshot: JsonNode = objectMapper.readTree(content)

                            dataStreamServiceRequest =
                                JSON.decodeFromString(
                                    DataStreamServiceRequest.Serializer, snapshot.toString(),
                                )

                            zipEntry = zipInputStream.nextEntry
                        }
                    }
                }

                Files.deleteIfExists(tempFile)
            } catch (e: IOException) {
                LOGGER.error("Error extracting files from zip", e)
                throw IOException("Invalid zip file content", e)
            }

            return@withContext dataStreamServiceRequest
        }

    override val dataFileName = "data-streams.json"

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ) = withContext(Dispatchers.IO) {
        val dataStreamIds =
            dataStreamIdRepository.getAllByDeploymentIds(
                deploymentIds.map { it.toString() },
            )

        dataStreamSequenceRepository.findAllByDataStreamIds(
            dataStreamIds,
        )
    }
}
