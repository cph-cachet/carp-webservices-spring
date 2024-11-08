package dk.cachet.carp.webservices.study.service.impl

import dk.cachet.carp.webservices.account.service.AccountService
import io.mockk.*
import org.junit.jupiter.api.Nested
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role
import kotlin.test.*
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration


class RecruitmentServiceWrapperTest {
    private val accountService: AccountService = mockk()
    private val dataStreamService: DataStreamService = mockk()
    val services: CoreServiceContainer = mockk<CoreServiceContainer> {
        every { recruitmentService } returns mockk<RecruitmentServiceDecorator>()
    }

    @Nested
    inner class InviteResearcher {
        @Test
        fun `researcher is invited if account does not exist`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val mockEmail = "test@gmail.com"
                val mockAccount = Account(
                    role = Role.RESEARCHER,
                )

                coEvery { accountService.findByAccountIdentity(ofType<EmailAccountIdentity>()) } returns null
                coEvery { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns Unit
                coEvery { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns mockAccount
                coEvery { accountService.grant(ofType<EmailAccountIdentity>(), any()) } returns mockAccount

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                sut.inviteResearcher(mockStudyId, mockEmail)

                coVerify { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
                coVerify(exactly = 0) { accountService.addRole(ofType<EmailAccountIdentity>(), any()) }
                coVerify { accountService.grant(ofType<EmailAccountIdentity>(), setOf(Claim.ManageStudy(mockStudyId))) }
            }
        }

        @Test
        fun `researcher is given role if account exists and has lower role`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val mockEmail = "test@gmail.com"
                val mockAccount = Account(
                    role = Role.PARTICIPANT,
                )

                coEvery { accountService.findByAccountIdentity(ofType<EmailAccountIdentity>()) } returns mockAccount
                coEvery { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns Unit
                coEvery { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns mockAccount
                coEvery { accountService.grant(ofType<EmailAccountIdentity>(), any()) } returns mockAccount

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                sut.inviteResearcher(mockStudyId, mockEmail)

                coVerify(exactly = 0) { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
                coVerify { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
                coVerify { accountService.grant(ofType<EmailAccountIdentity>(), setOf(Claim.ManageStudy(mockStudyId))) }
            }
        }

        @Test
        fun `throws if account has no role`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val mockEmail = "test@gmail.com"
                val mockAccount = Account(
                    role = null,
                )

                coEvery { accountService.findByAccountIdentity(ofType<EmailAccountIdentity>()) } returns mockAccount
                coEvery { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns Unit
                coEvery { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns mockAccount
                coEvery { accountService.grant(ofType<EmailAccountIdentity>(), any()) } returns mockAccount

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                assertFailsWith<NullPointerException> {
                    sut.inviteResearcher(mockStudyId, mockEmail)
                }

                coVerify(exactly = 0) { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
                coVerify(exactly = 0) { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
            }
        }

        @Test
        fun `invites researcher with no extras`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val mockEmail = "test@gmail.com"
                val mockAccount = Account(
                    role = Role.RESEARCHER,
                )

                coEvery { accountService.findByAccountIdentity(ofType<EmailAccountIdentity>()) } returns mockAccount
                coEvery { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns Unit
                coEvery { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns mockAccount
                coEvery { accountService.grant(ofType<EmailAccountIdentity>(), any()) } returns mockAccount

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                sut.inviteResearcher(mockStudyId, mockEmail)

                coVerify(exactly = 0) { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
                coVerify(exactly = 0) { accountService.addRole(ofType<EmailAccountIdentity>(), Role.RESEARCHER) }
                coVerify { accountService.grant(ofType<EmailAccountIdentity>(), setOf(Claim.ManageStudy(mockStudyId))) }
            }
        }
    }

    @Nested
    inner class RemoveResearcher {
        @Test
        fun `returns true if manages to remove researcher claims`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val mockEmail = "test@gmail.com"
                val mockAccount = Account(
                    role = Role.RESEARCHER, carpClaims = emptySet()
                )

                coEvery { accountService.revoke(ofType<EmailAccountIdentity>(), any()) } returns mockAccount

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                val result = sut.removeResearcher(mockStudyId, mockEmail)

                assertTrue(result)
            }
        }

        @Test
        fun `returns false if fails to remove researcher claims`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val mockEmail = "test@gmail.com"
                val mockAccount = Account(
                    role = Role.RESEARCHER, carpClaims = setOf(Claim.ManageStudy(mockStudyId))
                )

                coEvery { accountService.revoke(ofType<EmailAccountIdentity>(), any()) } returns mockAccount

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                val result = sut.removeResearcher(mockStudyId, mockEmail)

                assertFalse(result)
            }
        }
    }

    @Nested
    inner class GetParticipants {
        @Test
        fun `returns participants when offset and limit are not specified`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val offset = 0;
                val limit = 0;

                val ai1 = EmailAccountIdentity("ai1@gmail.com")
                val ai2 = EmailAccountIdentity("ai2@gmail.com")
                val ai3 = EmailAccountIdentity("ai3@gmail.com")

                val p1 = Participant(ai1)
                val p2 = Participant(ai2)
                val p3 = Participant(ai3)

                val a1 = Account(email = ai1.emailAddress.address)
                val a2 = Account(email = ai2.emailAddress.address)
                val a3 = Account(email = ai3.emailAddress.address)

                val mockParticipants = listOf(p1, p2, p3)

                coEvery { services.recruitmentService.getParticipants(mockStudyId) } returns mockParticipants
                coEvery { accountService.findByAccountIdentity(ai1) } returns a1
                coEvery { accountService.findByAccountIdentity(ai2) } returns a2
                coEvery { accountService.findByAccountIdentity(ai3) } returns a3

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                val result = sut.getParticipants(mockStudyId, offset, limit)

                assertEquals(mockParticipants.size, result.size)
                assertEquals(result.get(0), a1)
                assertEquals(result.get(1), a2)
                assertEquals(result.get(2), a3)
            }
        }

        @Test
        fun `returns participants when some participant account is not found`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val offset = 0;
                val limit = 0;

                val ai1 = EmailAccountIdentity("ai1@gmail.com")
                val ai2 = EmailAccountIdentity("ai2@gmail.com")
                val ai3 = EmailAccountIdentity("ai3@gmail.com")

                val p1 = Participant(ai1)
                val p2 = Participant(ai2)
                val p3 = Participant(ai3)

                val a1 = Account(email = ai1.emailAddress.address)
                val a2 = Account(email = ai2.emailAddress.address)
                val a3 = Account(email = ai3.emailAddress.address)

                val mockParticipants = listOf(p1, p2, p3)

                coEvery { services.recruitmentService.getParticipants(mockStudyId) } returns mockParticipants
                coEvery { accountService.findByAccountIdentity(ai1) } returns a1
                coEvery { accountService.findByAccountIdentity(ai2) } returns null
                coEvery { accountService.findByAccountIdentity(ai3) } returns a3

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                val result = sut.getParticipants(mockStudyId, offset, limit)

                assertEquals(mockParticipants.size, result.size)
                assertEquals(result.get(0), a1)
                assertEquals(result.get(1), a2)
                assertEquals(result.get(2), a3)
            }
        }

        @Test
        fun `returns participants with applied offset and limit`() {
            runTest {
                val mockStudyId = UUID.randomUUID()
                val offset = 2;
                val limit = 1;

                val ai1 = EmailAccountIdentity("ai1@gmail.com")
                val ai2 = EmailAccountIdentity("ai2@gmail.com")
                val ai3 = EmailAccountIdentity("ai3@gmail.com")

                val p1 = Participant(ai1)
                val p2 = Participant(ai2)
                val p3 = Participant(ai3)

                val a1 = Account(email = ai1.emailAddress.address)
                val a2 = Account(email = ai2.emailAddress.address)
                val a3 = Account(email = ai3.emailAddress.address)

                val mockParticipants = listOf(p1, p2, p3)

                coEvery { services.recruitmentService.getParticipants(mockStudyId) } returns mockParticipants
                coEvery { accountService.findByAccountIdentity(ai1) } returns a1
                coEvery { accountService.findByAccountIdentity(ai2) } returns a2
                coEvery { accountService.findByAccountIdentity(ai3) } returns a3

                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

                val result = sut.getParticipants(mockStudyId, offset, limit)

                assertEquals(1, result.size)
                assertEquals(result.get(0), a3)
            }
        }
    }

//    @Nested
//    inner class GetInactiveDeployments {
//        @Test
//        fun `inactive deployments are returned`() {
//            runTest {
//                val mockStudyId = UUID.randomUUID()
//
//                val mockSdId1 = UUID.randomUUID()
//                val mockSds1 = mockk<StudyDeploymentStatus>()
//                coEvery { mockSds1.studyDeploymentId } returns mockSdId1
//                val pg1 = ParticipantGroupStatus.InDeployment.fromDeploymentStatus(emptySet(), mockSds1)
//                val mockInstant1 = Instant.fromEpochSeconds(0)
//
//                val mockSdId2 = UUID.randomUUID()
//                val mockSds2 = mockk<StudyDeploymentStatus>()
//                coEvery { mockSds2.studyDeploymentId } returns mockSdId2
//                val pg2 = ParticipantGroupStatus.InDeployment.fromDeploymentStatus(emptySet(), mockSds2)
//                val mockInstant2 = Instant.fromEpochSeconds(0)
//
//                val mockParticipantGroupStatusList = listOf(pg1, pg2)
//
//                coEvery { services.recruitmentService.getParticipantGroupStatusList(mockStudyId) } returns mockParticipantGroupStatusList
//
//                coEvery { dataStreamService.getLatestUpdatedAt(mockSdId1) } returns mockInstant1
//                coEvery { dataStreamService.getLatestUpdatedAt(mockSdId2) } returns mockInstant2
//
//                val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)
//
//                val result = sut.getInactiveDeployments(mockStudyId, 0, 0, 0)
//
//                assertEquals(2, result.size)
//                assertEquals(result.get(0).dateOfLastDataUpload, mockInstant1)
//                assertEquals(result.get(1).dateOfLastDataUpload, mockInstant2)
//            }
//        }
//    }
}
