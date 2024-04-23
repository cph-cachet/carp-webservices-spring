package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.springframework.stereotype.Service
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Service
class IDataStreamServiceImpl(
        private val dataStreamSequenceRepository: DataStreamSequenceRepository,
        private val dataStreamIdRepository: DataStreamIdRepository
): IDataStreamService {

    override fun getDataStream(deploymentIds: List<String>): List<DataStreamSequence> {
        return dataStreamSequenceRepository.findAllByDeploymentIds(deploymentIds)
    }

    override fun getLatestUpdatedAt(deploymentId: UUID): Instant?
    {
        val dataStreamInputs = dataStreamIdRepository.getAllByDeploymentId(
            deploymentId.toString())
        val sortedDataPoint = dataStreamInputs.sortedByDescending {it.updatedAt}.firstOrNull()
            ?: return null

        return sortedDataPoint.updatedAt?.toKotlinInstant()
    }
/*

    override fun fromZipToBatch(studyDeploymentId: UUID, zipFile: File): DataStreamSequence<Sequence> {
        val zip = extractJsonFromZip(zipFile)

        return zip?.let {
            val dataStreamSequence = DataStreamSequence(
                dataStreamId = dataStreamIdRepository.findByDeploymentId(studyDeploymentId.toString())?.id,
                snapshot = it
            )
            dataStreamSequenceRepository.save(dataStreamSequence)
        }

            ?: throw IOException("No JSON file found in the zip.")
    }

    @Throws(IOException::class)
    fun extractJsonFromZip(zipFile: File): String? {
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".json")) {
                    // Found a JSON file, extract its content
                    val jsonContent = ByteArrayOutputStream()
                    zis.copyTo(jsonContent)
                    return jsonContent.toString(Charsets.UTF_8)
                }
                entry = zis.nextEntry
            }
        }
        // No JSON file found in the zip
        return null
    }
*/

}