package dk.cachet.carp.webservices.export.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.exception.responses.ConflictException
import dk.cachet.carp.webservices.export.command.ExportCommand
import dk.cachet.carp.webservices.export.command.ExportCommandInvoker
import dk.cachet.carp.webservices.export.domain.Export
import dk.cachet.carp.webservices.export.domain.ExportStatus
import dk.cachet.carp.webservices.export.repository.ExportRepository
import dk.cachet.carp.webservices.export.service.impl.ExportServiceImpl
import dk.cachet.carp.webservices.file.service.FileStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ExportServiceTest {
    private val repository = mockk<ExportRepository>()
    private val invoker = mockk<ExportCommandInvoker>()
    private val fileStorage = mockk<FileStorage>()

    @Nested
    inner class CreateExport {
        private val command = mockk<ExportCommand>()

        @Test
        fun `should return existing if found`() {
            val id = UUID.randomUUID().stringRepresentation
            val studyId = UUID.randomUUID().stringRepresentation

            val existingEntry =
                Export(
                    id = id,
                    studyId = studyId,
                    status = ExportStatus.AVAILABLE,
                )

            val newEntry =
                Export(
                    id = id,
                    studyId = studyId,
                    status = ExportStatus.IN_PROGRESS,
                )

            every { command.entry } returns newEntry
            every { repository.findByIdAndStudyId(id, studyId) } returns existingEntry

            val sut = ExportServiceImpl(repository, invoker, fileStorage)
            sut.createExport(command)

            verify(exactly = 0) { repository.save(any()) }
            verify(exactly = 0) { invoker.invoke(any()) }
        }

        @Test
        fun `should invoke command and save entry`() {
            val entry =
                Export(
                    id = UUID.randomUUID().stringRepresentation,
                    studyId = UUID.randomUUID().stringRepresentation,
                    status = ExportStatus.IN_PROGRESS,
                )

            every { command.entry } returns entry
            every { repository.findByIdAndStudyId(entry.id, entry.studyId) } returns null
            every { invoker.invoke(command) } answers { nothing }
            every { repository.save(entry) } returns entry

            val sut = ExportServiceImpl(repository, invoker, fileStorage)
            sut.createExport(command)

            verify { invoker.invoke(command) }
            verify { repository.save(entry) }
        }
    }

    @Nested
    inner class DeleteExport {
        @Test
        fun `should throw if export is in progress`() {
            val id = UUID.randomUUID()
            val studyId = UUID.randomUUID()

            val entry =
                Export(
                    id = id.stringRepresentation,
                    studyId = studyId.stringRepresentation,
                    status = ExportStatus.IN_PROGRESS,
                )

            every { repository.findByIdAndStudyId(any(), any()) } returns entry

            val sut = ExportServiceImpl(repository, invoker, fileStorage)
            assertFailsWith<ConflictException> { sut.deleteExport(id, studyId) }

            verify(exactly = 0) { repository.delete(any()) }
            verify(exactly = 0) { fileStorage.deleteFile(any()) }
        }

        @Test
        fun `should throw if entry doesn't exist`() {
            val id = UUID.randomUUID()
            val studyId = UUID.randomUUID()

            every { repository.findByIdAndStudyId(any(), any()) } returns null

            val sut = ExportServiceImpl(repository, invoker, fileStorage)
            assertFailsWith<IllegalArgumentException> { sut.deleteExport(id, studyId) }

            verify(exactly = 0) { repository.delete(any()) }
            verify(exactly = 0) { fileStorage.deleteFile(any()) }
        }

        @Test
        fun `should delete export and file`() {
            val id = UUID.randomUUID()
            val studyId = UUID.randomUUID()

            val entry =
                Export(
                    id = id.stringRepresentation,
                    studyId = studyId.stringRepresentation,
                    status = ExportStatus.AVAILABLE,
                )

            every { repository.findByIdAndStudyId(any(), any()) } returns entry
            every { repository.delete(entry) } answers { nothing }
            every { fileStorage.deleteFile(entry.fileName) } returns true

            val sut = ExportServiceImpl(repository, invoker, fileStorage)
            sut.deleteExport(id, studyId)

            verify { repository.delete(entry) }
            verify { fileStorage.deleteFile(entry.fileName) }
        }
    }
}
