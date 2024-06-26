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
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

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

    /**
     * This function processes a zip file and invokes the appropriate service method based on the extracted data.
     * It follows these steps:
     * 1. Converts the zip file to a byte array.
     * 2. Extracts a `DataStreamServiceRequest` from the byte array.
     * 3. If the extraction is successful, it processes the request.
     * 4. If the extraction fails, it throws an `IllegalArgumentException`.
     *
     * @param zipFile The zip file to be processed.
     * @return The result of invoking the service method with the extracted request.
     * @throws IllegalArgumentException If the zip file content is invalid.
     */

    override suspend fun processZipToInvoke(zipFile: MultipartFile): Any {
        val zipFileBytes = zipFile.bytes

        val dataStreamServiceRequest =
            extractFilesFromZip(zipFileBytes)
                ?: throw IllegalArgumentException("Invalid zip file content")

        return processRequest(dataStreamServiceRequest)
    }

    /**
     * This function is responsible for extracting a `DataStreamServiceRequest` from a zipped file.
     * It operates in the IO dispatcher context to optimize input/output operations and offload blocking operations.
     *
     * The function follows these steps:
     * 1. Writes the input ByteArray (representing a zipped file) to a temporary file.
     * 2. Opens the temporary zip file and iterates over each entry.
     * 3. Reads the content of each entry into a ByteArray and parses it into a `JsonNode`.
     * 4. Decodes the `JsonNode` into a `DataStreamServiceRequest`.
     * 5. Deletes the temporary file.
     *
     * IO dispatcher needed for file operations to optimize input/output operations and offloading blocking operations.
     *
     * If any error occurs during these operations, it logs the error and rethrows the exception.
     *
     * @param zipFile The ByteArray representing the zipped file.
     * @return The `DataStreamServiceRequest` extracted from the zipped file, or null if no request could be extracted.
     * @throws IOException If an error occurs during file operations.
     */

    suspend fun extractFilesFromZip(zipFile: ByteArray): DataStreamServiceRequest<*>? =
        withContext(Dispatchers.IO) {
            val objectMapper = ObjectMapper()
            var dataStreamServiceRequest: DataStreamServiceRequest<*>? = null

            try {
                // Write the ByteArray to a temporary file
                val tempFile = Files.createTempFile(null, null)
                Files.write(tempFile, zipFile, StandardOpenOption.WRITE)

                val fileSystem = FileSystem.SYSTEM
                val zipPath = tempFile.toAbsolutePath().toString().toPath()

                val zipFileSystem = fileSystem.openZip(zipPath)
                zipFileSystem.list("/".toPath()).forEach { pathInZip ->
                    zipFileSystem.source(pathInZip).use { source ->
                        val content = source.buffer().readByteArray()
                        val snapshot: JsonNode = objectMapper.readTree(content)

                        dataStreamServiceRequest =
                            JSON.decodeFromString(
                                DataStreamServiceRequest.Serializer, snapshot.toString(),
                            )
                    }
                }

                // Delete the temporary file
                Files.deleteIfExists(tempFile)
            } catch (e: IOException) {
                LOGGER.error("Error extracting files from zip", e)
                throw e
            }

            return@withContext dataStreamServiceRequest
        }

    /**
     * This function is responsible for processing a `DataStreamServiceRequest`.
     * It checks the type of the request and, based on its type, calls the appropriate core service method.
     *
     * The function follows these steps:
     * 1. Checks if the `DataStreamServiceRequest` is of type `AppendToDataStreams`.
     * 2. If it is, it invokes the core service method with the request.
     * 3. If the request is not of the correct type, it throws an `IllegalArgumentException`.
     *
     * @param dataStreamServiceRequest The `DataStreamServiceRequest` to be processed.
     * @return The result of invoking the core service method with the request.
     * @throws IllegalArgumentException If the request is not of the correct type.
     */
    suspend fun processRequest(dataStreamServiceRequest: DataStreamServiceRequest<*>): Any {
        return when (dataStreamServiceRequest) {
            is DataStreamServiceRequest.AppendToDataStreams -> {
                core.invoke(dataStreamServiceRequest)
            }
            else -> {
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
