package dk.cachet.carp.webservices.security.authentication.oauth2.issuers

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.KeycloakFacade
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

// KeycloakFacade is most likely to change when the official keycloak wrappers are released.
@OptIn(ExperimentalCoroutinesApi::class)
class KeycloakFacadeTest {

    private val realm = "test"
    private val clientId = "test"
    private val clientSecret = "test"
    private val objectMapper = mockk<ObjectMapper>()
    private val environmentUtil = mockk<EnvironmentUtil>()

    @Test
    fun `updateAccount is not supported`() = runTest {
        assertThrows<UnsupportedOperationException> {
            KeycloakFacade("", realm, clientId, clientSecret, objectMapper, environmentUtil).updateAccount(mockk())
        }
    }

    @Test
    fun `deleteAccount is not supported`() = runTest {
        assertThrows<UnsupportedOperationException> {
            KeycloakFacade("", realm, clientId, clientSecret, objectMapper, environmentUtil).deleteAccount("test")
        }
    }
}