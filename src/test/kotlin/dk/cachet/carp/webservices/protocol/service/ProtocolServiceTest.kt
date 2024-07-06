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
import kotlin.test.*

class ProtocolServiceTest {
    val accountService: AccountService = mockk()
    val protocolRepository: ProtocolRepository = mockk()
    val objectMapper: ObjectMapper = mockk()
    val services: CoreServiceContainer = mockk()

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

        // TODO: Fix this test, temporary disabled due to unable to mock StudyProtocolSnapshot
//        @Test
//        fun `should return protocol overview`() =
//            runTest {
//                val now = Clock.System.now()
//                val yesterday = now.minus(1.toDuration(DurationUnit.DAYS))
//                val id = UUID.randomUUID()
//
//                val versions =
//                    listOf(
//                        mockk<Protocol> {
//                            every { versionTag } returns "version 1"
//                            every { snapshot } returns "1"
//                            every { createdAt } returns yesterday.toJavaInstant()
//                        },
//                        mockk<Protocol> {
//                            every { versionTag } returns "version 2"
//                            every { snapshot } returns "1"
//                            every { createdAt } returns now.toJavaInstant()
//                        },
//                    )
//
//                val snapshot =
//                    mockk<StudyProtocolSnapshot> {
//                        every { ownerId } returns id
//                    }
//                val account =
//                    mockk<Account> {
//                        every { fullName } returns "John Doe"
//                    }
//
//                every { protocolRepository.findAllByIdSortByCreatedAt("id") } returns versions
//                every { objectMapper.treeToValue(any(), any<Class<*>>()) } returns snapshot
//                coEvery { accountService.findByUUID(any()) } returns account
//                val sut = ProtocolServiceWrapper(accountService, protocolRepository, services)
//
//                every { sut.decodeSnapshot("1") } returns snapshot
//                val result = sut.getSingleProtocolOverview("id")
//
//                assertNotNull(result)
//                assertEquals("John Doe", result.ownerName)
//                assertEquals("version 2", result.versionTag)
//                assertEquals(yesterday.toJavaInstant(), result.firstVersionCreatedDate)
//                assertEquals(now.toJavaInstant(), result.lastVersionCreatedDate)
//            }
    }
}
