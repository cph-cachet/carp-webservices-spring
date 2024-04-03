package dk.cachet.carp.webservices.study.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.*
import dk.cachet.carp.common.application.users.ParticipantRole
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith


@OptIn(ExperimentalCoroutinesApi::class)
class CoreRecruitmentServiceTest {
    private val coreStudyService: CoreStudyService = mockk()
    private val studyService: StudyService = mockk()
    private val coreDeploymentService: CoreDeploymentService = mockk()
    private val deploymentService: DeploymentService = mockk()

    @BeforeTest
    fun setup() {
        // setup event bus
        val eventBus = mockk<ApplicationServiceEventBus<RecruitmentService, RecruitmentService.Event>>()
        every { eventBus.subscribe(any<EventSubscriptionBuilder.() -> Unit>()) } returns mockk()
        mockkStatic(EventBus::createApplicationServiceAdapter)
        every { any<EventBus>().createApplicationServiceAdapter(eq(RecruitmentService::class)) } returns eventBus

        // setup singletons
        every { coreStudyService getProperty "instance" } answers { studyService }
        every { coreDeploymentService getProperty "instance" } answers { deploymentService }
    }

    @Nested
    inner class CreateAnonymousParticipants {
        @Test
        fun `should throw if there is no protocol set for the study`() = runTest {
            val studyDetails = mockk<StudyDetails>()

            every { studyDetails getProperty "protocolSnapshot" } returns null
            coEvery { studyService.getStudyDetails(any()) } returns studyDetails

            val sut = CoreRecruitmentService(
                mockk(),
                mockk(),
                coreDeploymentService,
                coreStudyService,
                mockk(),
                mockk(),
                mockk(),
            )

            assertFailsWith<IllegalArgumentException> {
                sut.createAnonymousParticipants(UUID.randomUUID(), 1, 1, "roleName")
            }

            coVerify(exactly = 1) { studyService.getStudyDetails(any()) }
        }

        @Test
        fun `should throw if the participant role name is not part of the protocol`() = runTest {
            val studyDetails = mockk<StudyDetails>()
            val protocolSnapshot = mockk<StudyProtocolSnapshot>()
            val roles = setOf(ParticipantRole("role", false))

            every { protocolSnapshot getProperty "participantRoles" } returns roles
            every { studyDetails getProperty "protocolSnapshot" } returns protocolSnapshot
            coEvery { studyService.getStudyDetails(any()) } returns studyDetails

            val sut = CoreRecruitmentService(
                mockk(),
                mockk(),
                coreDeploymentService,
                coreStudyService,
                mockk(),
                mockk(),
                mockk(),
            )

            assertFailsWith<IllegalArgumentException> {
                sut.createAnonymousParticipants(UUID.randomUUID(), 1, 1, "notInProtocol")
            }

            coVerify(exactly = 1) { studyService.getStudyDetails(any()) }
        }
    }
}