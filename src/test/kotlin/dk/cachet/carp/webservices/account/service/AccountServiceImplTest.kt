package dk.cachet.carp.webservices.account.service

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.account.service.impl.AccountServiceImpl
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authorization.Role
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

@OptIn(ExperimentalCoroutinesApi::class)
class AccountServiceImplTest {

    private val issuerFacade: IssuerFacade = mockk()
    private val accountFactory: AccountFactory = mockk()

    @Nested
    inner class Invite {
        @Test
        fun `should not create an account if one exists`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            val invitedAs = Role.RESEARCHER

            val foundAccount = mockk<Account>()
            every { foundAccount getProperty "role" } answers { callOriginal() }
            every { foundAccount setProperty "role" value any<Role>() } answers { callOriginal() }

            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } returns foundAccount
            coEvery { issuerFacade.createAccount(any()) } just runs
            coEvery { issuerFacade.addRole(any(), any()) } just runs
            coEvery { issuerFacade.sendInvitation(any(), any(), any()) } just runs

            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val invitedAccount = sut.invite(accountIdentity, invitedAs)

            coVerify(exactly = 1) { issuerFacade.getAccount(accountIdentity) }
            coVerify(exactly = 0) { issuerFacade.createAccount(any()) }
            coVerify(exactly = 1) { issuerFacade.addRole(foundAccount, invitedAs) }
            coVerify(exactly = 1) { issuerFacade.sendInvitation(foundAccount, null, false) }
            expect(invitedAs) { invitedAccount.role }
        }

        @Test
        fun `should create an account if none exists`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            val invitedAs = Role.RESEARCHER

            val foundAccount = null
            val createdAccount = mockk<Account>()
            every { createdAccount getProperty "role" } answers { callOriginal() }
            every { createdAccount setProperty "role" value any<Role>() } answers { callOriginal() }

            coEvery {
                issuerFacade.getAccount(any<AccountIdentity>())
            } answers {
                foundAccount
            } andThenAnswer {
                createdAccount
            }
            coEvery { issuerFacade.createAccount(any()) } just runs
            coEvery { issuerFacade.addRole(any(), any()) } just runs
            coEvery { issuerFacade.sendInvitation(any(), any(), any()) } just runs
            every { accountFactory.fromAccountIdentity(any()) } returns createdAccount

            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val invitedAccount = sut.invite(accountIdentity, invitedAs)

            coVerify(exactly = 2) { issuerFacade.getAccount(accountIdentity) }
            coVerify(exactly = 1) { issuerFacade.createAccount(any()) }
            coVerify(exactly = 1) { issuerFacade.addRole(any(), invitedAs) }
            coVerify(exactly = 1) { issuerFacade.sendInvitation(any(), null, true) }
            expect(invitedAs) { invitedAccount.role }
        }

        @Test
        fun `should throw if account creation fails`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            val invitedAs = Role.RESEARCHER

            val foundAccount = null
            val createdAccount = null

            coEvery {
                issuerFacade.getAccount(any<AccountIdentity>())
            } answers {
                foundAccount
            } andThenAnswer {
                createdAccount
            }
            coEvery { issuerFacade.createAccount(any()) } just runs
            coEvery { issuerFacade.addRole(any(), any()) } just runs
            coEvery { issuerFacade.sendInvitation(any(), any(), any()) } just runs
            every { accountFactory.fromAccountIdentity(any()) } returns mockk()

            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            assertFailsWith<IllegalArgumentException> {
                sut.invite(accountIdentity, invitedAs)
            }

            coVerify(exactly = 2) { issuerFacade.getAccount(accountIdentity) }
            coVerify(exactly = 1) { issuerFacade.createAccount(any()) }
            coVerify(exactly = 0) { issuerFacade.addRole(any(), invitedAs) }
            coVerify(exactly = 0) { issuerFacade.sendInvitation(any(), any(), any()) }
        }
    }

    @Nested
    inner class FindByUUID {
        @Test
        fun `should relay the task to issuerFacade`() = runTest {
            val uuid = UUID.randomUUID()
            val account = mockk<Account>()
            coEvery { issuerFacade.getAccount(any<UUID>()) } returns account
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val result = sut.findByUUID(uuid)

            coVerify(exactly = 1) { issuerFacade.getAccount(uuid) }
            expect(account) { result }
        }

        @Test
        fun `should return null if exception occurred`() = runTest {
            val uuid = UUID.randomUUID()
            coEvery { issuerFacade.getAccount(any<UUID>()) } throws Exception()
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val result = sut.findByUUID(uuid)

            coVerify(exactly = 1) { issuerFacade.getAccount(uuid) }
            expect(null) { result }
        }
    }

    @Nested
    inner class FindByAccountIdentity {
        @Test
        fun `should relay the task to issuerFacade`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            val account = mockk<Account>()
            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } returns account
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val result = sut.findByAccountIdentity(accountIdentity)

            coVerify(exactly = 1) { issuerFacade.getAccount(accountIdentity) }
            expect(account) { result }
        }

        @Test
        fun `should return null if exception occurred`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } throws Exception()
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val result = sut.findByAccountIdentity(accountIdentity)

            coVerify(exactly = 1) { issuerFacade.getAccount(accountIdentity) }
            expect(null) { result }
        }
    }

    @Nested
    inner class HasRoleByEmail {
        @Test
        fun `should relay the task to issuerFacade`() = runTest {
            val emailAddress = mockk<EmailAddress> {
                every { address } returns ""
            }
            val shouldNotHave = Role.RESEARCHER
            val shouldHave = Role.PARTICIPANT
            val account = mockk<Account>()
            every { account getProperty "role" } answers { Role.PARTICIPANT }
            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } returns account
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            val firstCall = sut.hasRoleByEmail(emailAddress, shouldNotHave)
            val secondCall = sut.hasRoleByEmail(emailAddress, shouldHave)

            coVerify(exactly = 2) { issuerFacade.getAccount(any<AccountIdentity>()) }
            expect(false) { firstCall }
            expect(true) { secondCall }
        }

        @Test
        fun `should throw if account is null`() = runTest {
            val emailAddress = mockk<EmailAddress> {
                every { address } returns ""
            }
            val role = Role.RESEARCHER
            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } returns null
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            assertFailsWith<IllegalArgumentException> {
                sut.hasRoleByEmail(emailAddress, role)
            }

            coVerify(exactly = 1) { issuerFacade.getAccount(any<AccountIdentity>()) }
        }
    }

    @Nested
    inner class AddRole {
        @Test
        fun `should throw if there isn't a matching account in the issuerFacade`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            val role = Role.RESEARCHER
            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } returns null
            coEvery { issuerFacade.addRole(any(), any()) } just runs
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            assertFailsWith<IllegalArgumentException> {
                sut.addRole(accountIdentity, role)
            }

            coVerify(exactly = 1) { issuerFacade.getAccount(accountIdentity) }
            coVerify(exactly = 0) { issuerFacade.addRole(any(), any()) }
        }

        @Test
        fun `should add role to the found account`() = runTest {
            val accountIdentity = mockk<AccountIdentity>()
            val role = Role.RESEARCHER
            val mockAccount = mockk<Account>()
            coEvery { issuerFacade.getAccount(any<AccountIdentity>()) } returns mockAccount
            coEvery { issuerFacade.addRole(any(), any()) } just runs
            val sut = AccountServiceImpl(issuerFacade, accountFactory)

            sut.addRole(accountIdentity, role)

            coVerify(exactly = 1) { issuerFacade.getAccount(accountIdentity) }
            coVerify(exactly = 1) { issuerFacade.addRole(mockAccount, role) }
        }
    }
}