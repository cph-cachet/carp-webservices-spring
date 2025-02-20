package dk.cachet.carp.webservices.datastream.service.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.data.application.DataStreamId
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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Path

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
            } catch (e: IllegalArgumentException) {
                LOGGER.error("An error occurred while storing the file (empty dataStreamList) ${path.fileName}", e)
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
