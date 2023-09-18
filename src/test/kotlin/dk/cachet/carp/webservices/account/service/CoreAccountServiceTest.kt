package dk.cachet.carp.webservices.account.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.deployments.application.users.Participation
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.webservices.account.service.impl.CoreAccountService
import dk.cachet.carp.webservices.common.email.service.EmailInvitationService
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.security.authentication.domain.Account
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect
import dk.cachet.carp.common.domain.users.Account as CoreAccount

@OptIn(ExperimentalCoroutinesApi::class)
class CoreAccountServiceTest
{
    private val accountService: AccountService = mockk()
    private val deploymentRepository: CoreDeploymentRepository = mockk()
    private val emailInvitationService: EmailInvitationService = mockk()

    @Nested
    inner class FindAccount {
        @Test
        fun `should try to find an account in accountService and return null when not found`() = runTest {
            val foundAccount = null
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )
            coEvery { accountService.findByAccountIdentity(any()) } returns foundAccount

            val result = sut.findAccount( mockk() )

            expect( null ) { result }
            coVerify(exactly = 1) { accountService.findByAccountIdentity(any()) }
        }

        @Test
        fun `should try to find account in accountService and return a CoreAccount if found`() = runTest {
            val foundAccount = Account(id = UUID.randomUUID().stringRepresentation)
            val accountIdentity = AccountIdentity.fromEmailAddress("")
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )
            coEvery { accountService.findByAccountIdentity(any()) } returns foundAccount

            val result = sut.findAccount( accountIdentity )

            expect( CoreAccount( accountIdentity, UUID.parse(foundAccount.id!!) ) ) { result }
            coVerify(exactly = 1) { accountService.findByAccountIdentity(any()) }
        }
    }

    @Nested
    inner class InviteExistingAccount {
        @Test
        fun `should throw if there are no deployments for the studyDeploymentId`() = runTest {
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns null
            coEvery { accountService.findByUUID(any()) } returns mockk()
            coEvery { accountService.invite(any(), any(), any()) } returns mockk()
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            assertFailsWith<IllegalArgumentException> {
                sut.inviteExistingAccount( UUID.randomUUID(), mockk(), mockParticipation, mockk() )
            }

            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 0) { accountService.findByUUID(any()) }
            coVerify(exactly = 0) { accountService.invite(any(), any(), any()) }
            verify(exactly = 0) { emailInvitationService.inviteToStudy(any(), any(), any())  }
        }

        @Test
        fun `should throw if there is no account for the accountId`() = runTest {
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            coEvery { accountService.findByUUID(any()) } returns null
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns mockk()
            coEvery { accountService.invite(any(), any(), any()) } returns mockk()
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            assertFailsWith<IllegalArgumentException> {
                sut.inviteExistingAccount( UUID.randomUUID(), mockk(), mockParticipation, mockk() )
            }

            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 1) { accountService.findByUUID(any()) }
            coVerify(exactly = 0) { accountService.invite(any(), any(), any()) }
            verify(exactly = 0) { emailInvitationService.inviteToStudy(any(), any(), any())  }
        }

        @Test
        fun `should throw if there is an account but there is no email for it`() = runTest {
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            val foundAccount = mockk<Account> {
                every { email } returns null
            }
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns mockk()
            coEvery { accountService.findByUUID(any()) } returns foundAccount
            coEvery { accountService.invite(any(), any(), any()) } returns mockk()
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            assertFailsWith<IllegalArgumentException> {
                sut.inviteExistingAccount( UUID.randomUUID(), mockk(), mockParticipation, mockk() )
            }

            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 1) { accountService.findByUUID(any()) }
            coVerify(exactly = 0) { accountService.invite(any(), any(), any()) }
            verify(exactly = 0) { emailInvitationService.inviteToStudy(any(), any(), any())  }
        }

        @Test
        fun `should find the account and send an invitation to it`() = runTest {
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            val foundAccount = mockk<Account> {
                every { email } returns "email"
            }
            val foundProtocol = mockk<StudyProtocol> {
                every { primaryDevices } returns setOf()
                every { tasks } returns setOf()
            }
            val foundDeployment = mockk<StudyDeployment> {
                every { protocol } returns foundProtocol
            }
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns foundDeployment
            coEvery { accountService.findByUUID(any()) } returns foundAccount
            coEvery { accountService.invite(any(), any(), any()) } returns mockk()
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            sut.inviteExistingAccount( UUID.randomUUID(), mockk(), mockParticipation, mockk() )

            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 1) { accountService.findByUUID(any()) }
            coVerify(exactly = 1) { accountService.invite(any(), any(), any()) }
            verify(exactly = 1) { emailInvitationService.inviteToStudy(any(), any(), any())  }
        }
    }


    @Nested
    inner class InviteNewAccount {

        @Test
        fun `should throw if the identity is not EmailAccountIdentity`() = runTest {
            val accountIdentity = AccountIdentity.fromUsername("")

            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            assertFailsWith<IllegalArgumentException> {
                sut.inviteNewAccount( accountIdentity, mockk(), mockk(), mockk() )
            }
        }

        @Test
        fun `should throw if there are no deployments for the studyDeploymentId`() = runTest {
            val accountIdentity = AccountIdentity.fromEmailAddress("")
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            val foundDeployment = null
            val invitedAccount = mockk<Account>()
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns foundDeployment
            coEvery { accountService.findByAccountIdentity(any()) } returns null
            coEvery { accountService.invite(any(), any()) } returns invitedAccount
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            assertFailsWith<IllegalArgumentException> {
                sut.inviteNewAccount( accountIdentity, mockk(), mockParticipation, mockk() )
            }

            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 0) { accountService.findByAccountIdentity(any()) }
            coVerify(exactly = 0) { accountService.invite(any(), any()) }
            verify(exactly = 0) { emailInvitationService.inviteToStudy(any(), any(), any())  }
        }

        @Test
        fun `should throw if there already is an account with the given identity`() = runTest {
            val accountIdentity = AccountIdentity.fromEmailAddress("")
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            val foundDeployment = mockk<StudyDeployment> {
                every { protocol } returns mockk()
            }
            val foundAccount = mockk<Account>()
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns foundDeployment
            coEvery { accountService.findByAccountIdentity(any()) } returns foundAccount
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            assertFailsWith<IllegalArgumentException> {
                sut.inviteNewAccount( accountIdentity, mockk(), mockParticipation, mockk() )
            }
            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 1) { accountService.findByAccountIdentity(any()) }
            coVerify(exactly = 0) { accountService.invite(any(), any()) }
            verify(exactly = 0) { emailInvitationService.inviteToStudy(any(), any(), any())  }
        }

        @Test
        fun `should send an invitation to the account`() = runTest {
            val accountIdentity = AccountIdentity.fromEmailAddress("")
            val mockParticipation = mockk<Participation> {
                every { studyDeploymentId } returns UUID.randomUUID()
            }
            val foundProtocol = mockk<StudyProtocol> {
                every { primaryDevices } returns setOf()
                every { tasks } returns setOf()
            }
            val foundDeployment = mockk<StudyDeployment> {
                every { protocol } returns foundProtocol
            }
            val invitedAccount = mockk<Account>() {
                every { id } returns UUID.randomUUID().toString()
            }
            coEvery { deploymentRepository.getStudyDeploymentBy(any()) } returns foundDeployment
            coEvery { accountService.invite(any(), any()) } returns invitedAccount
            coEvery { accountService.findByAccountIdentity(any()) } returns null
            every { emailInvitationService.inviteToStudy(any(), any(), any()) } returns mockk()
            val sut = CoreAccountService( accountService, deploymentRepository, emailInvitationService )

            val coreAccount = sut.inviteNewAccount( accountIdentity, mockk(), mockParticipation, mockk() )

            coVerify(exactly = 1) { deploymentRepository.getStudyDeploymentBy(any()) }
            coVerify(exactly = 1) { accountService.findByAccountIdentity(any()) }
            coVerify(exactly = 1) { accountService.invite(any(), any()) }
            verify(exactly = 1) { emailInvitationService.inviteToStudy(any(), any(), any())  }
            expect( CoreAccount( accountIdentity, UUID.parse(invitedAccount.id!!) ) ) { coreAccount }
        }
    }
}