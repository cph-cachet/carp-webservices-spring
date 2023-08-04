package dk.cachet.carp.webservices.account.authorization

import dk.cachet.carp.webservices.account.domain.AccountRequest
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Role
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.expect

class AccountAuthorizationServiceTest
{
    private val authenticationService: AuthenticationService = mockk()
    private var mockAccount: Account? = null

    @BeforeTest
    fun setup() {
        mockAccount = mockk()
        every { authenticationService.getCurrentPrincipal() } returns mockAccount!!
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Nested
    inner class CanAccountInvite {
        @Test
        fun `should return false if the requester is a participant`() {
            // arrange
            val requestedRole = Role.UNKNOWN
            val requesterRole = Role.PARTICIPANT
            val accountRequest = AccountRequest("", requestedRole)
            val sut = AccountAuthorizationService(mockk(), mockk(), mockk(), mockk(), authenticationService)
            every { mockAccount!!.role } returns requesterRole

            // act
            val result = sut.canInvite(accountRequest)

            // assert
            expect(false) { result }
            verify(exactly = 1) { authenticationService.getCurrentPrincipal() }
        }

        @Test
        fun `should return false if the requester is a RESEARCHER but is less privileged than the requested`() {
            // arrange
            val requestedRole = Role.SYSTEM_ADMIN
            val requesterRole = Role.RESEARCHER
            val accountRequest = AccountRequest("", requestedRole)
            val sut = AccountAuthorizationService(mockk(), mockk(), mockk(), mockk(), authenticationService)
            every { mockAccount!!.role } returns requesterRole

            // act
            val result = sut.canInvite(accountRequest)

            // assert
            expect(false) { result }
            verify(exactly = 1) { authenticationService.getCurrentPrincipal() }
        }

        @Test
        fun `should return true if the requester is at least a RESEARCHER and is the same or more privileged than the requested`() {
            // arrange
            val requestedRole = Role.RESEARCHER
            val requesterRole = Role.RESEARCHER
            val accountRequest = AccountRequest("", requestedRole)
            val sut = AccountAuthorizationService(mockk(), mockk(), mockk(), mockk(), authenticationService)
            every { mockAccount!!.role } returns requesterRole

            // act
            val result = sut.canInvite(accountRequest)

            // assert
            expect(true) { result }
        }
    }
}