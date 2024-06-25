package dk.cachet.carp.webservices.data.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.data.serdes.DataStreamServiceRequestSerializer
import dk.cachet.carp.webservices.data.service.core.CoreDataStreamService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DataStreamServiceWrapperTest {
    @Nested
    inner class ExtractFilesFromZip {
        @Test
        fun `should extract files from valid zip`() =
            runTest {
                // Arrange
                // Mock the DataStreamServiceRequest
                val dataStreamServiceRequest = DataStreamServiceRequest.AppendToDataStreams(
                    studyDeploymentId = UUID.randomUUID(),
                    batch = CawsMutableDataStreamBatchWrapper()
                )

                every { dataStreamServiceRequest.apiVersion } returns ApiVersion(1, 2)

                // Convert the mock to a byte array
                val objectMapper = ObjectMapper()

                val messageBase = mockk<MessageBase>()
                every { messageBase.get(any(), any()) } returns "Serializer fails somewhere"

                val serializer = DataStreamServiceRequestSerializer(messageBase)
                val requestAsJson = objectMapper.writeValueAsString(serializer.serialize(dataStreamServiceRequest, mockk(), mockk()))
                val requestBytes = requestAsJson.toByteArray()

                // Zip the byte array
                val byteArrayOutputStream = ByteArrayOutputStream()
                val zipOutputStream = ZipOutputStream(byteArrayOutputStream)

                zipOutputStream.putNextEntry(ZipEntry("request.json"))
                zipOutputStream.write(requestBytes)
                zipOutputStream.closeEntry()
                zipOutputStream.close()

                // Get the zipped byte array
                val zippedRequestBytes = byteArrayOutputStream.toByteArray()

                val services = mockk<CoreServiceContainer>()
                val coreDataStreamService = mockk<CoreDataStreamService>()
                every { services.dataStreamService } returns DataStreamServiceDecorator(coreDataStreamService, mockk())

                // Mock the behavior of zipFile here
                val sut =
                    DataStreamServiceWrapper(
                        mockk(),
                        mockk(),
                        services,
                    )

                // Act & Assert
                assertDoesNotThrow {
                    sut.extractFilesFromZip(zippedRequestBytes)
                }
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
