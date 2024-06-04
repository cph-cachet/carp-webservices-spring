package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.application.DataStreamBatch
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
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class DataStreamServiceWrapper(
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository,
    services: CoreServiceContainer,
) : DataStreamService, ResourceExporter<DataStreamSequence<Any?>> {
    final override val core = services.dataStreamService

    override val dataFileName: String = "data-streams.json" // Implementing the required property

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

    override fun extractFilesFromZip(studyDeploymentId: UUID, zipFile: ByteArray): DataStreamBatch {
        TODO("Not yet implemented")
    }

    /*    override val dataFileName = "data-streams.json"
        override val typeA =
            override val typeB =*/

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ) = withContext(Dispatchers.IO) {
        dataStreamSequenceRepository.findAllByDeploymentIds(
            deploymentIds.map { it.toString() },
        )
    }

/*    override fun extractFilesFromZip(studyDeploymentId: UUID, zipFile: ByteArray): CawsMutableDataStreamBatch {
        val zipInputStream = ZipInputStream(ByteArrayInputStream(zipFile))
        val objectMapper = ObjectMapper()
        val batch = CawsMutableDataStreamBatch()

        var entry: ZipEntry? = zipInputStream.nextEntry
        var sequenceId = 1L

        while (entry != null) {
            if (!entry.isDirectory) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    byteArrayOutputStream.write(buffer, 0, len)
                }
                val content = byteArrayOutputStream.toByteArray()
                val snapshot: JsonNode = objectMapper.readTree(content)

                val jsonString = snapshot.toString()

                val dataStreamSequence: DataStreamSequenceA = JSON.decodeFromString(jsonString)

*//*
                batch.appendSequence(dataStreamSequence)
*//*
                sequenceId++

*//*                val dataType = snapshot.get("__type").asText()

                val dataStreamSequence: DataStreamSequenceA = when (dataType) {
                    DATA_TYPE -> JSON.decodeFromString<DataStreamSequenceA>(jsonString)
*//**//*
                    "TypeB" -> JSON.decodeFromString<DataStreamSequenceB>(jsonString)
*//**//*
                    else -> throw IllegalArgumentException("Unknown data type: $dataType")
                }

                batch.appendSequence(dataStreamSequence)
                sequenceId++*//*
            }
            entry = zipInputStream.nextEntry
        }
        return batch
    }*/






/*    override fun extractFilesFromZip(studyDeploymentId: UUID, zipFile: ByteArray): MutableDataStreamBatch {
        val zipInputStream = ZipInputStream(ByteArrayInputStream(zipFile))
        val objectMapper = ObjectMapper()
        val batch = MutableDataStreamBatch()

        var entry: ZipEntry? = zipInputStream.nextEntry
        var sequenceId = 1L

        while (entry != null) {
            if (!entry.isDirectory) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    byteArrayOutputStream.write(buffer, 0, len)
                }
                val content = byteArrayOutputStream.toByteArray()
                val snapshot: JsonNode = objectMapper.readTree(content)

                val dataStreamSequence: DataStreamSequence<Any> = JSON.decodeFromString<>(snapshot.toString())

                batch.appendSequence(dataStreamSequence)

                sequenceId++
            }
            entry = zipInputStream.nextEntry
        }
        return batch
    }*/
}
