package dk.cachet.carp.webservices.study.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.infrastructure.StudyServiceDecorator
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.study.service.impl.RecruitmentServiceImpl
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith


class RecruitmentServiceTest
{
    private var coreStudyService: StudyServiceDecorator = mockk()
    private val serviceContainer: CoreServiceContainer = mockk()

    @BeforeTest
    fun setup()
    {
        clearAllMocks()
        every { serviceContainer.studyService } returns coreStudyService
        every { serviceContainer.recruitmentService } returns mockk()
    }

    @Nested
    inner class CreateAnonymousParticipants {
        @Test
        fun `should throw if there is no protocol set for the study`() = runTest {
            val studyDetails = mockk<StudyDetails>()

            every { studyDetails getProperty "protocolSnapshot" } returns null
            coEvery { coreStudyService.getStudyDetails(any()) } returns studyDetails

            val sut = RecruitmentServiceImpl(
                mockk(),
                mockk(),
                serviceContainer
            )

            assertFailsWith<IllegalArgumentException> {
                sut.addAnonymousParticipants(
                    UUID.randomUUID(),
                    1,
                    1,
                    "roleName",
                    "redirect"
                )
            }

            coVerify(exactly = 1) { coreStudyService.getStudyDetails(any()) }
        }

        @Test
        fun `should throw if the participant role name is not part of the protocol`() = runTest {
            val studyDetails = mockk<StudyDetails>()
            val protocolSnapshot = mockk<StudyProtocolSnapshot>()
            val roles = setOf(ParticipantRole("role", false))

            every { protocolSnapshot getProperty "participantRoles" } returns roles
            every { studyDetails getProperty "protocolSnapshot" } returns protocolSnapshot
            coEvery { coreStudyService.getStudyDetails(any()) } returns studyDetails

            val sut = RecruitmentServiceImpl(
                mockk(),
                mockk(),
                serviceContainer
            )

            assertFailsWith<IllegalArgumentException> {
                sut.addAnonymousParticipants(
                    UUID.randomUUID(),
                    1,
                    1,
                    "notInProtocol",
                    "redirect")
            }

            coVerify(exactly = 1) { coreStudyService.getStudyDetails(any()) }
        }
    }
}