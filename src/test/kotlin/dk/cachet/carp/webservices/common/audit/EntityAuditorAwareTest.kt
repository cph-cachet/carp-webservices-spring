package dk.cachet.carp.webservices.common.audit

import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import io.mockk.*
import kotlin.test.Test
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals

class EntityAuditorAwareTest {
    private val authenticationService: AuthenticationService = mockk()

    @Nested
    inner class GetCurrentAuditor {
        @Test
        fun `should return id of authenticated user if available`() {
            every { authenticationService.getId().stringRepresentation } returns "user"

            val sut = EntityAuditorAware(authenticationService)

            val auditor = sut.getCurrentAuditor()
            assertEquals(auditor.get(), "user")
        }


        @Test
        fun `should return SYSTEM if no authenticated user is available`() {
            every { authenticationService.getId() } throws Exception("No user authenticated")

            val sut = EntityAuditorAware(authenticationService)

            val auditor = sut.getCurrentAuditor()
            assertEquals(auditor.get(), "SYSTEM")
        }
    }
}