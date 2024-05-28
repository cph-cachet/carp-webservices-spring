package dk.cachet.carp.webservices.export.command.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import dk.cachet.carp.webservices.export.command.ExportCommand
import dk.cachet.carp.webservices.export.domain.Export
import dk.cachet.carp.webservices.export.service.ResourceExporterService
import dk.cachet.carp.webservices.file.util.FileUtil
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively

class ExportSummary(
    entry: Export,
    private val deploymentIds: Set<UUID>?,
    private val resourceExporter: ResourceExporterService,
    private val fileUtil: FileUtil,
) : ExportCommand(entry) {
    override fun canExecute(): Boolean = true

    @OptIn(ExperimentalPathApi::class)
    override suspend fun execute() {
        val workingDir = createTempDirectory()
        val zipPath = fileUtil.resolveFileStorage(entry.fileName)

        resourceExporter.exportStudyData(UUID(entry.studyId), deploymentIds, workingDir, logger)

        try {
            fileUtil.zipDirectory(workingDir, zipPath)
        } catch (e: Throwable) {
            fileUtil.deleteFile(zipPath)
            throw FileStorageException(e.message)
        } finally
        {
            workingDir.deleteRecursively()
        }
    }
}
