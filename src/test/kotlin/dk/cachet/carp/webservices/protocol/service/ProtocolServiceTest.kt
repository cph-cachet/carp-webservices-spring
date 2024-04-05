package dk.cachet.carp.webservices.protocol.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.protocol.domain.Protocol
import dk.cachet.carp.webservices.protocol.repository.ProtocolRepository
import dk.cachet.carp.webservices.protocol.service.impl.ProtocolServiceImpl
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
import kotlin.test.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ProtocolServiceTest
{
    val accountService: AccountService = mockk()
    val protocolRepository: ProtocolRepository = mockk()
    val objectMapper: ObjectMapper = mockk()
    val services: CoreServiceContainer = mockk()

    @BeforeEach
    fun setup()
    {
        clearAllMocks()
        every { services.protocolService } returns mockk()
    }

    @Nested
    inner class GetSingleProtocolOverview
    {
        @Test
        fun `should return null if there are no versions for the id`() = runTest {
            every { protocolRepository.findAllByIdSortByCreatedAt( any() ) } returns emptyList()

            val sut = ProtocolServiceImpl( accountService, protocolRepository, mockk(), services )

            assertNull( sut.getSingleProtocolOverview( "id" ) )
        }

        @Test
        fun `should return protocol overview`() = runTest {
            val now = Clock.System.now()
            val yesterday = now.minus( 1.toDuration( DurationUnit.DAYS ) )
            val id = UUID.randomUUID()

            val versions = listOf(
                mockk<Protocol> {
                    every { versionTag } returns "version 1"
                    every { snapshot } returns mockk<JsonNode>()
                    every { createdAt } returns yesterday.toJavaInstant()
                },
                mockk<Protocol> {
                    every { versionTag } returns "version 2"
                    every { snapshot } returns mockk<JsonNode>()
                    every { createdAt } returns now.toJavaInstant()
                }
            )

            val snapshot = mockk<StudyProtocolSnapshot> {
                every { ownerId } returns id
            }
            val account = mockk<Account> {
                every { firstName } returns "John"
                every { lastName } returns "Doe"
            }

            every { protocolRepository.findAllByIdSortByCreatedAt( "id" ) } returns versions
            every { objectMapper.treeToValue( any(), any<Class<*>>()) } returns snapshot
            coEvery { accountService.findByUUID( any() ) } returns account

            val sut = ProtocolServiceImpl( accountService, protocolRepository, objectMapper, services )

            val result = sut.getSingleProtocolOverview( "id" )

            assertNotNull( result )
            assertEquals( "John Doe", result.ownerName )
            assertEquals( "version 2", result.versionTag )
            assertEquals( yesterday.toJavaInstant(), result.firstVersionCreatedDate )
            assertEquals( now.toJavaInstant(), result.lastVersionCreatedDate )
        }
    }
}