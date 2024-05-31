package dk.cachet.carp.webservices.export.command

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.export.command.impl.ExportAnonymousParticipants
import dk.cachet.carp.webservices.export.domain.Export
import dk.cachet.carp.webservices.export.service.ResourceExporterService
import dk.cachet.carp.webservices.file.util.FileUtil
import dk.cachet.carp.webservices.study.domain.AnonymousParticipantRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFalse

class ExportAnonymousParticipantsTest {
    private val services = mockk<CoreServiceContainer>()
    private val accountService = mockk<AccountService>()
    private val resourceExporter = mockk<ResourceExporterService>()
    private val fileUtil = mockk<FileUtil>()

    private val payload =
        AnonymousParticipantRequest(
            amountOfAccounts = 1,
            expirationSeconds = 1L,
            redirectUri = "uri",
            participantRoleName = "role",
        )

    @Nested
    inner class CanExecute {
        private val entry =
            mockk<Export> {
                every { studyId } returns UUID.randomUUID().stringRepresentation
            }

        @Test
        fun `should return false if there is no protocol`() {
            every { services.studyService } returns
                mockk {
                    coEvery { getStudyDetails(any()) } returns
                        mockk {
                            every { protocolSnapshot } returns null
                        }
                }

            val command =
                ExportAnonymousParticipants(
                    entry,
                    payload,
                    services,
                    accountService,
                    resourceExporter,
                    fileUtil,
                )
            val canExecute = command.canExecute()

            assertFalse(canExecute)
        }

        @Test
        fun `should return false if the participant role is not in the protocol`() {
            every { services.studyService } returns
                mockk {
                    coEvery { getStudyDetails(any()) } returns
                        mockk {
                            every { protocolSnapshot } returns
                                mockk {
                                    every { participantRoles } returns emptySet()
                                }
                        }
                }

            val command =
                ExportAnonymousParticipants(
                    entry,
                    payload,
                    services,
                    accountService,
                    resourceExporter,
                    fileUtil,
                )
            val canExecute = command.canExecute()

            assertFalse(canExecute)
        }

        @Test
        fun `should return false if the amount of accounts is not in the range`() {
            every { services.studyService } returns
                mockk {
                    coEvery { getStudyDetails(any()) } returns
                        mockk {
                            every { protocolSnapshot } returns
                                mockk {
                                    every { participantRoles } returns
                                        setOf(
                                            mockk {
                                                every { role } returns payload.participantRoleName
                                            },
                                        )
                                }
                        }
                }

            val command =
                ExportAnonymousParticipants(
                    entry,
                    payload.copy(amountOfAccounts = 0),
                    services,
                    accountService,
                    resourceExporter,
                    fileUtil,
                )
            val canExecute = command.canExecute()

            assertFalse(canExecute)
        }
    }
}
