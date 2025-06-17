package dk.cachet.carp.webservices.protocol.service

import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.protocol.domain.Protocol
import dk.cachet.carp.webservices.protocol.repository.ProtocolRepository
import dk.cachet.carp.webservices.protocol.service.impl.ProtocolServiceWrapper
import dk.cachet.carp.webservices.security.authentication.domain.Account
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.protocols.domain.StudyProtocol
import kotlinx.datetime.Instant

class ProtocolServiceTest {
    val accountService: AccountService = mockk()
    val protocolRepository: ProtocolRepository = mockk()
    val services: CoreServiceContainer = mockk()
    val objectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { services.protocolService } returns mockk()
    }

    @Nested
    inner class GetSingleProtocolOverview {
        @Test
        fun `should return null if there are no versions for the id`() = runTest {
            every { protocolRepository.findAllByIdSortByCreatedAt(any()) } returns emptyList()

            val sut = ProtocolServiceWrapper(accountService, protocolRepository, services)

            assertNull(sut.getSingleProtocolOverview("id"))
        }
    }
}
