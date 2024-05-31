package dk.cachet.carp.webservices.data.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.application.MutableDataStreamBatch
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Suppress("UNREACHABLE_CODE", "LABEL_NAME_CLASH")
@Service
class DataStreamServiceWrapper(
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository,
    services: CoreServiceContainer,
) : DataStreamService, ResourceExporter<DataStreamSequence> {
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

    override fun extractFilesFromZip(studyDeploymentId: UUID, zipFile: ByteArray): MutableDataStreamBatch {
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
    }
}
