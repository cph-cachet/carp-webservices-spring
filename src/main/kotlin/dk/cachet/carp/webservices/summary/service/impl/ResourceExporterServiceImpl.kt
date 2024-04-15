package dk.cachet.carp.webservices.summary.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.webservices.collection.service.ICollectionService
import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import dk.cachet.carp.webservices.consent.service.IConsentDocumentService
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.dataPoint.service.DataPointService
import dk.cachet.carp.webservices.deployment.repository.StudyDeploymentRepository
import dk.cachet.carp.webservices.deployment.service.core.CoreParticipationService
import dk.cachet.carp.webservices.document.domain.Document
import dk.cachet.carp.webservices.document.service.IDocumentService
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.service.FileService
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.file.util.FileUtil
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import dk.cachet.carp.webservices.summary.domain.SummaryLog
import dk.cachet.carp.webservices.summary.service.ResourceExporterService
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

@Service
class ResourceExporterServiceImpl
(
    private val objectMapper: ObjectMapper,
    private val studyRepository: CoreStudyRepository,
    private val studyDeploymentRepository: StudyDeploymentRepository,
    coreParticipationService: CoreParticipationService,
    private val dataPointService: DataPointService,
    private val consentDocumentService: IConsentDocumentService,
    private val fileService: FileService,
    private val fileStorage: FileStorage,
    private val fileUtil: FileUtil,
    private val documentService: IDocumentService,
    private val collectionService: ICollectionService,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository
): ResourceExporterService
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    private val deploymentParticipationService: ParticipationService = coreParticipationService.instance

    /**
     * Exports every application resource serialized into the specified [rootFolder].
     *
     * @param studyId The ID of the study that needs to be exported.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override suspend fun exportAllForStudy(studyId: UUID, deploymentIds: List<String>?, rootFolder: Path, summaryLog: SummaryLog)
    {
        val studyDeploymentIds = deploymentIds ?: studyRepository.getDeploymentIdsOrThrow(studyId)

        exportStudy(studyId, rootFolder, summaryLog)
        exportDeployments(studyDeploymentIds, rootFolder, summaryLog)
        exportParticipantData(studyDeploymentIds, rootFolder, summaryLog)
        exportDataPoints(studyDeploymentIds, rootFolder, summaryLog)
        exportDataStreamSequences(studyDeploymentIds, rootFolder, summaryLog)
        exportConsents(studyDeploymentIds, rootFolder, summaryLog)
        exportFiles(studyId, studyDeploymentIds, rootFolder, summaryLog)
        exportDocuments(studyId, studyDeploymentIds, rootFolder, summaryLog)
    }


    /**
     * Exports the study serialized into the specified [rootFolder].
     *
     * @param studyId The ID of the study that needs to be exported.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override suspend fun exportStudy(studyId: UUID, rootFolder: Path, summaryLog: SummaryLog)
    {
        val study = studyRepository.getStudySnapshotById(studyId)
        val studyPath = resolveFullPathForFilename("${rootFolder.fileName}/study.json")
        createFileForResourceOnPath(studyPath, study, summaryLog)
    }

    /**
     * Exports the study deployments serialized into the specified [rootFolder].
     *
     * @param studyId The ID of the study that needs to be exported.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportDeployments(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)
    {
        val deployments = studyDeploymentRepository.findAllByStudyDeploymentIds(deploymentIds.toSet())
                .map { objectMapper.treeToValue(it.snapshot, StudyDeploymentSnapshot::class.java) }

        if (deployments.isEmpty())
        {
            LOGGER.info("No deployments were found.")
            summaryLog.infoLogs.add("No deployments were found.")
            return
        }
        val deploymentsPath = resolveFullPathForFilename("${rootFolder.fileName}/deployments.json")
        createFileForResourceOnPath(deploymentsPath, deployments, summaryLog)
    }

    /**
     * Exports the participant data serialized into the specified [rootFolder].
     *
     * @param deploymentIds ID's of the deployments the resources belong to.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportParticipantData(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog) = runBlocking {
        val participantDataList = mutableListOf<ParticipantData>()
        deploymentIds.forEach {
            val data = deploymentParticipationService.getParticipantData(UUID(it))
            participantDataList.add(data)
        }
        if (participantDataList.isEmpty())
        {
            LOGGER.info("No participant data was found.")
            summaryLog.infoLogs.add("No deployments was found.")
            return@runBlocking
        }
        val participantDataListPath = resolveFullPathForFilename("${rootFolder.fileName}/participant-data.json")
        createFileForResourceOnPath(participantDataListPath, participantDataList, summaryLog)
    }

    /**
     * Exports data points serialized into the specified [rootFolder].
     *
     * @param deploymentIds ID's of the deployments the resources belong to.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportDataPoints(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)
    {
        val dataPoints = dataPointService.getAllForDownload(deploymentIds)
        if (dataPoints.isEmpty())
        {
            LOGGER.info("No datapoint data was found.")
            summaryLog.infoLogs.add("No datapoint data was found.")
            return
        }
        val dataPointsPath = resolveFullPathForFilename("${rootFolder.fileName}/datapoints.json")
        createFileForResourceOnPath(dataPointsPath, dataPoints, summaryLog)
    }

    /**
     * Exports core data points serialized into the specified [rootFolder].
     *
     * @param deploymentIds ID's of the deployments the resources belong to.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportDataStreamSequences(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)
    {
        val dataStreamSequences = dataStreamSequenceRepository.findAllByDeploymentIds(deploymentIds)
        if (dataStreamSequences.isEmpty())
        {
            LOGGER.info("No dataStreamSequences was found.")
            summaryLog.infoLogs.add("No dataStreamSequences was found.")
            return
        }
        val dataPointsPath = resolveFullPathForFilename("${rootFolder.fileName}/data-streams.json")
        createFileForResourceOnPath(dataPointsPath, dataStreamSequences, summaryLog)
    }

    /**
     * Exports consent documents serialized into the specified [rootFolder].
     *
     * @param deploymentIds ID's of the deployments the resources belong to.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportConsents(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)
    {
        val consents = consentDocumentService.getAll(deploymentIds)
        if (consents.isEmpty())
        {
            LOGGER.info("No consent document data was found.")
            summaryLog.infoLogs.add("No consent document data was found.")
            return
        }
        val consentsPath = resolveFullPathForFilename("${rootFolder.fileName}/consent_documents.txt")
        createFileForResourceOnPath(consentsPath, consents, summaryLog)
    }

    /**
     * Exports files serialized into the specified [rootFolder].
     *
     * @param studyId ID of the study the files belong to.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportFiles(studyId: UUID, deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)
    {
        val files = mutableListOf<File>()
        deploymentIds.forEach {
            val data = fileService.getAllByStudyIdAndDeploymentId(studyId.stringRepresentation, it)
            files.addAll(data)
        }

        if (files.isEmpty())
        {
            LOGGER.info("No files were found.")
            summaryLog.infoLogs.add("No files were found.")
            return
        }
        val filesPath = resolveFullPathForFilename("${rootFolder.fileName}/files.json")
        createFileForResourceOnPath(filesPath, files, summaryLog)
        files.forEach {
            try
            {
                val resource = fileStorage.getResource(it.storageName)
                val copyPath = resolveFullPathForFilename("${rootFolder.fileName}/${it.originalName}")
                Files.copy(resource.file.toPath(), copyPath)
            }
            catch (ex: Exception)
            {
                summaryLog.errorLogs.add("An error occurred while exporting the file ${it.storageName}, error message: ${ex.message}")
                LOGGER.info("An error occurred while exporting the file ${it.storageName} (${it.originalName}) with id ${it.id}: ${ex.message}")
            }
        }
    }

    /**
     * Exports files serialized into the specified [rootFolder].
     *
     * @param studyId ID of the study the documents belong to.
     * @param rootFolder [Path] of the destination directory.
     * @param summaryLog A [SummaryLog] instance to audit meta data.
     */
    override fun exportDocuments(studyId: UUID, deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)
    {
        val documentsList = ArrayList<Document>()
        deploymentIds.forEach {
            val collections = collectionService.getAllByStudyIdAndDeploymentId(studyId.stringRepresentation, it)

            val documents = documentService.getAll(collections.map {  collection ->  collection.id })
            if (documents.isEmpty())
            {
                LOGGER.info("No document data was found for deploymentId: $it")
                summaryLog.infoLogs.add("No document data was found for deploymentId: $it")
                return
            }
            documentsList.addAll(documents)
        }

        val documentsPath = resolveFullPathForFilename("${rootFolder.fileName}/documents.txt")
        createFileForResourceOnPath(documentsPath, documentsList, summaryLog)
    }

    /**
     * Creates a new directory and returns its [Path]
     *
     * @return [Path]
     */
    override fun createNewDirectory(leafDirName: String): Path
    {
        val rootPath = resolveFullPathForFilename(leafDirName)

        try
        {
            Files.createDirectory(rootPath)
            LOGGER.info("Temporary directory for ${rootPath.fileName} is created.")
        } catch (ex: IOException)
        {
            LOGGER.info("Temporary directory for ${rootPath.fileName} cannot be created. Operation aborted. Exception: ${ex.message}")
            fileUtil.deleteDirectory(rootPath)
            throw FileStorageException(ex.message)
        }
        return rootPath
    }

    /**
     * Exports a [SummaryLog] instance serialized into the specified folder.
     *
     * @param summaryLog The [SummaryLog] to be exported.
     * @param rootFolder [Path] of the destination directory.
     */
    override fun exportSummaryLog(summaryLog: SummaryLog, rootFolder: Path)
    {
        val summaryLogPath = resolveFullPathForFilename("${rootFolder.fileName}/summary-logs.txt")
        createSummaryLogFileOnPath(summaryLogPath, summaryLog)
    }

    private fun resolveFullPathForFilename(fileName: String): Path
    {
        return fileUtil.resolveFileStorage(fileName)
    }

    private fun createFileForResourceOnPath(path: Path, resource: Any, summaryLog: SummaryLog) {
        try {
            val jsonGenerator = objectMapper.factory.createGenerator(path.toFile().outputStream())

            jsonGenerator.use {
                objectMapper.writeValue(it, resource)
            }

            LOGGER.info("A new file is created for zipping with name ${path.fileName}.")
        } catch (ex: Exception) {
            LOGGER.info("Failed to store the file ${path.fileName}.", ex)
            summaryLog.errorLogs.add("An error occurred while storing the file ${path.fileName}: ${ex.message}")
        }
    }


    private fun createSummaryLogFileOnPath(path: Path, summaryLog: SummaryLog)
    {
        try
        {
            val serializedResource = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summaryLog)
            val resourceStream = ByteArrayInputStream(serializedResource.encodeToByteArray())
            Files.copy(resourceStream, path)
            LOGGER.info("A new log file is created for zipping with name ${path.fileName}.")
        }
        catch (ex: Exception)
        {
            LOGGER.info("Failed to store log file ${path.fileName}.", ex)
        }
    }
}