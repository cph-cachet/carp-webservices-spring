package dk.cachet.carp.webservices.export.command

import dk.cachet.carp.webservices.export.domain.ExportStatus
import dk.cachet.carp.webservices.export.repository.ExportRepository
import io.mockk.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ExportCommandInvokerTest {
    private val repository = mockk<ExportRepository>()

    @Nested
    inner class Invoke {
        @Test
        fun `should require command to be executable`() {
            val notExecutableCommand =
                mockk<ExportCommand> {
                    every { canExecute() } returns false
                }

            val sut = ExportCommandInvokerImpl(repository)
            assertFailsWith<IllegalArgumentException> { sut.invoke(notExecutableCommand) }

            verify(exactly = 0) { repository.updateExportStatus(any(), any()) }
        }

        @Test
        fun `should execute command`() =
            runTest {
                // channel to signal the end of the coroutine
                val coroutineFinished = Channel<Boolean>()

                val command =
                    mockk<ExportCommand> {
                        every { canExecute() } returns true
                        coEvery { execute() } answers { nothing }
                        every { entry } returns
                            mockk {
                                every { id } returns "id"
                            }
                    }

                // This call marks the end of the coroutine
                coEvery { repository.updateExportStatus(any(), any()) } coAnswers {
                    coroutineFinished.send(true)
                }

                val sut = ExportCommandInvokerImpl(repository)
                sut.invoke(command)

                // wait for the coroutine to finish
                coroutineFinished.receive()
                coVerify(exactly = 1) { command.execute() }
                verify(exactly = 1) { repository.updateExportStatus(ExportStatus.AVAILABLE, any()) }
            }
    }
}
