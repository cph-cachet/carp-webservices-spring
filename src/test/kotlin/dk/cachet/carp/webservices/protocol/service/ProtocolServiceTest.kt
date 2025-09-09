package dk.cachet.carp.webservices.protocol.service

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.protocol.repository.ProtocolRepository
import dk.cachet.carp.webservices.protocol.service.impl.ProtocolServiceWrapper
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertNull

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
        fun `should return null if there are no versions for the id`() =
            runTest {
                every { protocolRepository.findAllByIdSortByCreatedAt(any()) } returns emptyList()

                val sut = ProtocolServiceWrapper(accountService, protocolRepository, services)

                assertNull(sut.getSingleProtocolOverview("id"))
            }
    }
}
