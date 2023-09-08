package dk.cachet.carp.webservices.summary.service

import dk.cachet.carp.webservices.summary.domain.SummaryLog
import java.nio.file.Path

/**
 * Functions to export serialized application data into the file system.
 */
interface IResourceExporterService
{
    /** Exports every application resource serialized into the specified [rootFolder]. */
    suspend fun exportAllForStudy(studyId: String, deploymentIds: List<String>?, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports the study serialized into the specified [rootFolder]. */
    suspend fun exportStudy(studyId: String, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports deployments serialized into the specified [rootFolder]. */
    fun exportDeployments(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports participant data serialized into the specified [rootFolder]. */
    fun exportParticipantData(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports data points serialized into the specified [rootFolder]. */
    fun exportDataPoints(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports data stream sequences serialized into the specified [rootFolder]. */
    fun exportDataStreamSequences(deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports files serialized into the specified [rootFolder]. */
    fun exportFiles(studyId: String, deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)

    /** Exports documents serialized into the specified [rootFolder]. */
    fun exportDocuments(studyId: String, deploymentIds: List<String>, rootFolder: Path, summaryLog: SummaryLog)

    /** Creates a new directory and returns its [Path]. */
    fun createNewDirectory(leafDirName: String): Path

    /** Exports [SummaryLog] serialized into the specified [rootFolder]. */
    fun exportSummaryLog(summaryLog: SummaryLog, rootFolder: Path)
}