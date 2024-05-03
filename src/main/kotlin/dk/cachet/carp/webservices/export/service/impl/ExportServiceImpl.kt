package dk.cachet.carp.webservices.export.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.exception.responses.ConflictException
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.export.command.ExportCommand
import dk.cachet.carp.webservices.export.command.ExportCommandInvoker
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.export.domain.Export
import dk.cachet.carp.webservices.export.domain.ExportStatus
import dk.cachet.carp.webservices.export.repository.ExportRepository
import dk.cachet.carp.webservices.export.service.ExportService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

@Service
class ExportServiceImpl(
    private val exportRepository: ExportRepository,
    private val exportCommandInvoker: ExportCommandInvoker,
    private val fileStorage: FileStorage,
) : ExportService
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun createExport( command: ExportCommand ): Export
    {
        val existing = getExportOrDeleteIfFailed( UUID( command.entry.id ), UUID( command.entry.studyId ) )

        if ( existing != null ) return existing

        exportCommandInvoker.invoke( command )

        return exportRepository.save( command.entry )
    }

    override fun downloadExport( studyId: UUID, exportId: UUID ): Resource
    {
        val export = getExportOrThrow( exportId, studyId )

        val file = fileStorage.getFile( export.fileName )

        LOGGER.info("Summary with id $studyId is being downloaded.")

        return file
    }

    override fun getAllForStudy( studyId: UUID ) = exportRepository.findAllByStudyId( studyId.stringRepresentation )

    override fun deleteExport( studyId: UUID, exportId: UUID ): UUID
    {
        val export = getExportOrThrow( exportId, studyId )

        if ( export.status == ExportStatus.IN_PROGRESS )
        {
            throw ConflictException( "The export creation is still in progress." )
        }

        fileStorage.deleteFile( export.fileName )
        exportRepository.delete( export )
        LOGGER.info( "Export with id $exportId has been successfully deleted." )
        return studyId
    }


    /**
     * Get the export with the given [id] if it exists, or delete it if it failed to export and return null.
     */
    private fun getExportOrDeleteIfFailed( id: UUID, studyId: UUID ): Export?
    {
        try
        {
            val export = getExportOrThrow( id, studyId )

            if ( export.status != ExportStatus.ERROR )
            {
                return export
            }

            exportRepository.deleteById( export.id )
        }
        catch ( _: Throwable )
        {
            LOGGER.info( "No export exists with id $id." )
        }

        return null
    }

    private fun getExportOrThrow( id: UUID, studyId: UUID ): Export
    {
        val export = exportRepository.findByIdAndStudyId( id.stringRepresentation, studyId.stringRepresentation )

        requireNotNull( export ) { "No export exists with id $id." }

        return export
    }
}