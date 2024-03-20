
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.webservices.protocol.domain.Protocol
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolOverviewResponseDto
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolResponseDto
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import dk.cachet.carp.webservices.protocol.repository.ProtocolRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProtocolServiceImplTest {
    @Test
    fun `test getLatestProtocols with empty repository`(): Unit = runTest {
        val protocolRepository: ProtocolRepository = mockk()
        every { protocolRepository.findAll() } returns emptyList()

        val coreProtocolRepository: CoreProtocolRepository = mockk()
        /*TODO here it fails, doesnt return List, since I use CoreProtocolService*/
        val result: List<Any?> = coreProtocolRepository.getLatestProtocols()
        assertEquals(emptyList<Any>(), result)
    }
    @Test
    fun `getLatestProtocols returns empty list when protocol snapshot is null`() = runTest {
        val protocolRepository: ProtocolRepository = mockk()
        every { protocolRepository.findAll() } returns mutableListOf(
            Protocol(versionTag = "snapshotId1", snapshot = null),
            Protocol(versionTag = "snapshotId2", snapshot = null)
        )

        val coreProtocolRepository: CoreProtocolRepository = mockk()
        every { coreProtocolRepository.getLatestProtocolById(any()) } returns null

        /*TODO here it ALSO fails, doesnt return List, since I use CoreProtocolService*/
        val result = coreProtocolRepository.getLatestProtocols()
        val initial = emptyList<GetLatestProtocolOverviewResponseDto>()

        assertEquals(initial, result)
    }

    @Test
    fun `test getLatestProtocols with protocols having the same protocol Id(snapshot) but different version tags`() = runTest {
        /*TODO rethink, its basically a test for a duplicate */

        // Define the shared ID
        val sameSnapshotId = "d1110a16-3812-44ec-93dc-6a2f3afa4a7e"
        val dummySnapshot: StudyProtocolSnapshot = mockk<StudyProtocolSnapshot>()

        // Create protocols with the same ID but different version tags
        val protocolWithSameIdDifferentVersion1 = Protocol(versionTag = "versionTag1", snapshot = null)
        val protocolWithSameIdDifferentVersion2 = Protocol(versionTag = "versionTag2", snapshot = null)

        // Create a protocol with a unique ID
        val protocolWithUniqueId = Protocol(versionTag = "uniqueVersionTag", snapshot = null)

        val protocolRepository: ProtocolRepository = mockk()
        every { protocolRepository.findAll() } returns listOf(
            protocolWithSameIdDifferentVersion1,
            protocolWithSameIdDifferentVersion2,
            protocolWithUniqueId
        )

        val coreProtocolRepository: CoreProtocolRepository = mockk()
        every { coreProtocolRepository.getLatestProtocolById(sameSnapshotId) } returns GetLatestProtocolResponseDto(
            versionTag = "versionTag2",
            snapshot = dummySnapshot,
            firstVersionCreatedDate = java.time.Instant
                    .now(),
            lastVersionCreatedDate = java.time.Instant
                    .now()
        )

        // Execute method under test
        val result = coreProtocolRepository.getLatestProtocols()

        // Validate result
        assertEquals(2, result.size) // Expecting 2 entries, one for each unique protocol with the same snapshot ID
        result[0]?.snapshot?.let { assertEquals(protocolWithSameIdDifferentVersion2.versionTag.toInt(), it.version) } // Ensure the latest version is returned
    }

}