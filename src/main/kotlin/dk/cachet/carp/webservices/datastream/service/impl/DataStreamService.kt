package dk.cachet.carp.webservices.datastream.service.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.domain.DataStreamSequence
import dk.cachet.carp.webservices.datastream.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.datastream.service.createSequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.dao.DataAccessException
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
    private val objectMapper: ObjectMapper,
    services: CoreServiceContainer,
) : DataStreamService {
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
        val dataStreamIds = findDataStreamIdsByDeploymentId(deploymentId)
        return findLatestUpdatedAtByDataStreamIds(dataStreamIds)
    }

    fun findDataStreamIdsByDeploymentId(deploymentId: UUID): List<Int> {
        return dataStreamIdRepository.getAllByDeploymentId(deploymentId.toString()).map { it.id }
    }

    fun findLatestUpdatedAtByDataStreamIds(dataStreamIds: List<Int>): Instant? {
        return if (dataStreamIds.isEmpty()) {
            null
        } else {
            dataStreamSequenceRepository.findMaxUpdatedAtByDataStreamIds(dataStreamIds)?.toKotlinInstant()
        }
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

    val dataFileName = "data-streams.json"

    suspend fun exportDataOrThrow(
        deploymentIds: Set<UUID>,
        target: Path,
    ): Unit =
        withContext(Dispatchers.IO) {
            val dataStreamIds =
                dataStreamIdRepository.getAllByDeploymentIds(
                    deploymentIds.map { it.toString() },
                )

            val path = target.resolve(dataFileName)

            try {
                getDataStreams(dataStreamIds, target)
                LOGGER.info("A new file is created for zipping with name ${path.fileName}.")
            } catch (e: IOException) {
                LOGGER.error("An error occurred while storing the file ${path.fileName}", e)
            }
        }

    private fun DataStreamSequence.toRange(): LongRange {
        return firstSequenceId!!..lastSequenceId!!
    }

    suspend fun getDataStreams(
        dataStreamIds: List<Int>,
        target: Path,
    ) = withContext(Dispatchers.IO) {
        // Validate inputs
        require(dataStreamIds.isNotEmpty()) { "DataStream list cannot be empty." }

        val path = target.resolve(dataFileName)

        val jsonGenerator = objectMapper.factory.createGenerator(path.toFile().outputStream())
        jsonGenerator.writeStartArray()

        val sequenceIds = dataStreamSequenceRepository.findSequenceIdsByStreamId(dataStreamIds)

        sequenceIds.map { sequenceId ->
            try {
                // Return empty if no sequences found
                val sequence = dataStreamSequenceRepository.findById(sequenceId).orElse(null)

                buildDataStreamBatch(sequence, jsonGenerator)
            } catch (e: IllegalArgumentException) {
                LOGGER.info(
                    "Failed to process dataStream " +
                        "$sequenceId: ${e.message}",
                )
            }
        }
        jsonGenerator.writeEndArray()
        jsonGenerator.close()
    }

    private fun buildDataStreamBatch(
        dataStreamSequence: DataStreamSequence,
        jsonGenerator: JsonGenerator,
    ) {
        val id =
            dataStreamIdRepository.findByDataStreamId(dataStreamSequence.dataStreamId!!)
        check(id != null) { "DataStreamId not found for ID: ${dataStreamSequence.dataStreamId}" }

        val dataStreamId =
            DataStreamId(
                studyDeploymentId =
                    UUID(
                        id.studyDeploymentId ?: error("StudyDeploymentId not found"),
                    ),
                deviceRoleName = id.deviceRoleName ?: error("DeviceRoleName is null"),
                dataType =
                    DataType(
                        namespace = id.nameSpace ?: error("NameSpace is null"),
                        name = id.name ?: error("Name is null"),
                    ),
            )

        try {
            val sequenceRange = dataStreamSequence.toRange()
            val sequence = createSequence(dataStreamId, dataStreamSequence, sequenceRange, objectMapper)

            val batch = MutableDataStreamBatchDecorator()
            batch.appendSequence(sequence)

            batch.toList().map { dataStreamPoint ->
                objectMapper.writeValue(jsonGenerator, dataStreamPoint)
            }
        } catch (e: IllegalStateException) {
            LOGGER.error("State error while processing sequence ID: ${dataStreamSequence.id} - ${e.message}", e)
        } catch (e: JsonProcessingException) {
            LOGGER.error("JSON serialization error for sequence ID: ${dataStreamSequence.id} - ${e.message}", e)
        } catch (e: DataAccessException) {
            LOGGER.error("Database access error for sequence ID: ${dataStreamSequence.id} - ${e.message}", e)
        }
    }
}
