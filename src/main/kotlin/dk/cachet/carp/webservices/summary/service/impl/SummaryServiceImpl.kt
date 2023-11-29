package dk.cachet.carp.webservices.summary.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import dk.cachet.carp.webservices.common.exception.responses.ConflictException
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.file.util.FileUtil
import dk.cachet.carp.webservices.summary.domain.Summary
import dk.cachet.carp.webservices.summary.domain.SummaryLog
import dk.cachet.carp.webservices.summary.domain.SummaryStatus
import dk.cachet.carp.webservices.summary.factory.impl.SummaryFactory
import dk.cachet.carp.webservices.summary.repository.SummaryRepository
import dk.cachet.carp.webservices.summary.service.IResourceExporterService
import dk.cachet.carp.webservices.summary.service.ISummaryService
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.util.concurrent.Executors

@Service
class SummaryServiceImpl
    (
    private val resourceExporter: IResourceExporterService,
    private val summaryRepository: SummaryRepository,
    private val summaryFactory: SummaryFactory,
    private val fileStorage: FileStorage,
    private val fileUtil: FileUtil,
) : ISummaryService {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
        private val threadPoolExecutor = Executors.newCachedThreadPool()
    }

    /**
     * NOTE: TODO this should depend on kotlin coroutines. Zip creation should be handled by the Dispatchers.IO context
     * Currently, the persistence layer that we use, doesn't provide native support for kotlin coroutines so we
     * should switch to something that does, like R2DBC. (The issue manifests in the fact the updates dont work in a reactive context)
     * */
    override fun createSummaryForStudy(studyId: String, deploymentIds: List<String>?): Summary {
        var summary = summaryFactory.create(UUID(studyId), deploymentIds)
        val existingSummary = getSummary(summary.id)

        if (existingSummary != null) {
            return existingSummary
        }

        summary = summaryRepository.save(summary)

        threadPoolExecutor.execute {
            LOGGER.info("Creating summary...")
            val summaryLog = SummaryLog(studyId, summary.createdAt)

            val path = resourceExporter.createNewDirectory(summary.id)

            try {
                exportStudyOrThrow(studyId, deploymentIds, path, summaryLog)
                zipStudyOrThrow(path, summary.fileName)
                resourceExporter.exportSummaryLog(summaryLog, path)

                summaryRepository.updateSummaryStatus(SummaryStatus.AVAILABLE, summary.id)
                LOGGER.info("New summary saved with file name: ${summary.fileName}.")
            } catch (ex: Exception) {
                summaryRepository.updateSummaryStatus(SummaryStatus.ERROR, summary.id)
                throw ex
            } finally {
                fileUtil.deleteDirectory(path)
            }
        }

        return summary
    }

    override fun downloadSummary(id: String): Resource {
        val summary = getSummaryById(id)
        val file = fileStorage.getFile(summary.fileName)

        summaryRepository.save(summary)
        LOGGER.info("Summary with id $id is being downloaded.")

        return file
    }

    override fun listSummaries(accountId: String, studyId: String?): List<Summary> {
        if (studyId.isNullOrEmpty()) {
            return summaryRepository.findAllByCreatedBy(accountId)
        }

        return summaryRepository.findAllByCreatedByAndStudyId(accountId, studyId)
    }

    override fun deleteSummaryById(id: String): String {
        val summary = getSummaryById(id)
        if (summary.status == SummaryStatus.IN_PROGRESS) {
            throw ConflictException("The summary creation is still in progress.")
        }

        fileStorage.deleteFile(summary.fileName)
        summaryRepository.delete(summary)
        LOGGER.info("Summary with id $id is successfully deleted.")
        return id
    }

    override fun getSummaryById(id: String): Summary {
        val summaryOptional = summaryRepository.findById(id)
        if (!summaryOptional.isPresent) {
            LOGGER.info("Summary with id $id is not found.")
            throw ResourceNotFoundException("Summary with id $id is not found.")
        }
        return summaryOptional.get()
    }

    private fun zipStudyOrThrow(path: Path, fileName: String) {
        try {
            fileUtil.zipDirectory(path, fileName)
        } catch (ex: Exception) {
            LOGGER.info("Zip operation failed due to an error: ${ex.message}")
            fileUtil.deleteFile(fileUtil.resolveFileStorage(fileName))
            throw FileStorageException(ex.message)
        }
    }

    private fun exportStudyOrThrow(studyId: String, deploymentIds: List<String>?, path: Path, log: SummaryLog) {
        try {
            runBlocking { resourceExporter.exportAllForStudy(studyId, deploymentIds, path, log) }
        } catch (ex: Exception) {
            LOGGER.info("Data collection failed due to an error: ${ex.message}")
            throw FileStorageException(ex.message)
        }
    }

    private fun getSummary(id: String): Summary? {
        try {
            val summary = getSummaryById(id)

            if (summary.status != SummaryStatus.ERROR) {
                return summary
            }

            summaryRepository.deleteById(summary.id)
        } catch (ex: Exception) {
            LOGGER.info("No summary exists with id $id.")
        }

        return null
    }
}