package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.data.service.core.CoreDataStreamService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DataStreamServiceWrapperTest {
    @Nested
    inner class ExtractFilesFromZip {
        @Test
        fun `should extract files from valid zip`() =
            runTest {
                // Arrange
                val zipFile = mockk<MultipartFile>().bytes
                // Mock the behavior of zipFile here
                val dataStreamServiceRequest = mockk<DataStreamServiceRequest<*>>()

                val sut =
                    DataStreamServiceWrapper(
                        mockk(),
                        mockk(),
                        mockk(),
                    )

                // Act
                val result = sut.extractFilesFromZip(zipFile)

                // Assert
                // Add your assertions here
                assertEquals(dataStreamServiceRequest, result)
            }

        @Test
        fun `should throw error for invalid zip`() =
            runTest {
                // Arrange
                val mockFile = mockk<MultipartFile>()
                every { mockFile.originalFilename } returns "test.txt"
                every { mockFile.contentType } returns "text/plain"
                every { mockFile.size } returns 100L
                every { mockFile.isEmpty } returns false
                every { mockFile.bytes } returns "test content".toByteArray()

                val services = mockk<CoreServiceContainer>()
                val coreDataStreamService = mockk<CoreDataStreamService>()
                every { services.dataStreamService } returns DataStreamServiceDecorator(coreDataStreamService, mockk())

                /*
                every { coreDataStreamService.appendToDataStreams(any(), any()) } returns Unit
*/


/*                val dataStreamServiceRequest = DataStreamServiceDecorator(coreDataStreamService, mockk())
                every { services.dataStreamService } returns dataStreamServiceRequest*/

                // Mock the behavior of zipFile here
                val sut =
                    DataStreamServiceWrapper(
                        mockk(),
                        mockk(),
                        services,
                    )

                // Act & Assert
                assertFailsWith<IOException> {
                    sut.extractFilesFromZip(mockFile.bytes)
                }
            }

        @Test
        fun `should return null for empty zip`() =
            runTest {
                // Arrange
                val zipFile = mockk<MultipartFile>()
                // Mock the behavior of zipFile here

                val sut =
                    DataStreamServiceWrapper(
                        mockk(),
                        mockk(),
                        mockk(),
                    )

                // Act
                val result = sut.extractFilesFromZip(zipFile.bytes)

                // Assert
                assertNull(result)
            }
       }
}
