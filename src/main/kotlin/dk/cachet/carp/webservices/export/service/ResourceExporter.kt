package dk.cachet.carp.webservices.export.service

import dk.cachet.carp.common.application.UUID
import java.nio.file.Path

/**
 * Makes a service as capable of exporting a specific type of data. This allows each service to define its own export
 * logic and be discovered by the [ResourceExporterService] automatically.
 *
 * @param TExport The type of data to export.
 */
interface ResourceExporter<out TExport> {
    val dataFileName: String

    suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ): Collection<TExport>
}
