package dk.cachet.carp.webservices.datastream.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.service.core.CoreDataStreamService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import kotlin.test.assertFailsWith

class DataStreamServiceTest {
    @Nested
    inner class ExtractFilesFromZip {
        @BeforeEach
        fun setup() {
            System.setProperty("kotlinx.coroutines.debug", "on")
        }

        @Test
        fun `should extract files from valid zip from generateRandomDataStreamServiceRequest()`() =
            runTest {
                val requestAsJson = generateRandomDataStreamServiceRequest()
                val requestBytes = requestAsJson.toByteArray()

                val byteArrayOutputStream = ByteArrayOutputStream()
                val zipOutputStream = ZipArchiveOutputStream(byteArrayOutputStream)

                val entry = ZipArchiveEntry("request.json")
                zipOutputStream.putArchiveEntry(entry)
                zipOutputStream.write(requestBytes)
                zipOutputStream.closeArchiveEntry()
                zipOutputStream.close()

                val zippedRequestBytes = byteArrayOutputStream.toByteArray()

                val services = mockk<CoreServiceContainer>()
                val coreDataStreamService = mockk<CoreDataStreamService>()
                every { services.dataStreamService } returns DataStreamServiceDecorator(coreDataStreamService, mockk())

                val sut =
                    DataStreamService(
                        mockk(),
                        mockk(),
                        mockk(),
                        mockk(),
                        services,
                    )

                assertDoesNotThrow {
                    withContext(Dispatchers.IO) {
                        sut.extractFilesFromZip(zippedRequestBytes)
                    }
                }
            }

        @Deprecated("This test is not valid anymore")
        fun `should extract files from valid zip with initialization of DataStreamRequest`() =
            runTest {
//                val studyDeploymentId = UUID.randomUUID()
//                val dataStreamBatch = MutableDataStreamBatchDecorator()
//                val dataStreamServiceRequest =
//                    DataStreamServiceRequest.AppendToDataStreams(studyDeploymentId, dataStreamBatch)

                val objectMapper = ObjectMapper()

                val messageBase = mockk<MessageBase>()
                every { messageBase.get(any(), any()) } returns "Serializer fails somewhere"

//                val serializer = DataStreamServiceRequestSerializer(messageBase)

                val writer = StringWriter()
                val jsonGenerator = objectMapper.createGenerator(writer)

//                serializer.serialize(dataStreamServiceRequest, jsonGenerator, mockk())
                jsonGenerator.flush()

                val requestAsJson = writer.toString()

                val requestBytes = requestAsJson.toByteArray()

                val byteArrayOutputStream = ByteArrayOutputStream()
                val zipOutputStream = ZipOutputStream(byteArrayOutputStream)

                zipOutputStream.putNextEntry(ZipEntry("request.json"))
                zipOutputStream.write(requestBytes)
                zipOutputStream.closeEntry()
                zipOutputStream.close()

                val zippedRequestBytes = byteArrayOutputStream.toByteArray()

                val services = mockk<CoreServiceContainer>()
                val coreDataStreamService = mockk<CoreDataStreamService>()
                every { services.dataStreamService } returns DataStreamServiceDecorator(coreDataStreamService, mockk())

                val sut =
                    DataStreamService(
                        mockk(),
                        mockk(),
                        mockk(),
                        mockk(),
                        services,
                    )

                assertDoesNotThrow {
                    withContext(Dispatchers.IO) {
                        sut.extractFilesFromZip(zippedRequestBytes)
                    }
                }
            }

        @Test
        fun `should throw error for invalid zip`() =
            runTest {
                val invalidJsonZip = createInvalidZip()

                val mockFile = mockk<MultipartFile>()
                every { mockFile.originalFilename } returns "invalid.zip"
                every { mockFile.contentType } returns "application/zip"
                every { mockFile.size } returns invalidJsonZip.size.toLong()
                every { mockFile.isEmpty } returns false
                every { mockFile.bytes } returns invalidJsonZip

                val services = mockk<CoreServiceContainer>()
                val coreDataStreamService = mockk<CoreDataStreamService>()
                every { services.dataStreamService } returns
                    DataStreamServiceDecorator(
                        coreDataStreamService,
                        mockk(),
                    )
                val sut =
                    DataStreamService(
                        mockk(),
                        mockk(),
                        mockk(),
                        mockk(),
                        services,
                    )

                assertFailsWith<IOException> {
                    withContext(Dispatchers.IO) {
                        sut.extractFilesFromZip(mockFile.bytes)
                    }
                }
            }

        private fun createInvalidZip(): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val zipOutputStream = ZipOutputStream(byteArrayOutputStream)

            zipOutputStream.putNextEntry(ZipEntry("invalid.json"))
            zipOutputStream.write("invalid json content".toByteArray())
            zipOutputStream.closeEntry()

            zipOutputStream.close()

            return byteArrayOutputStream.toByteArray()
        }

        private fun generateRandomDataStreamServiceRequest(): String {
            val objectMapper = ObjectMapper()

            val writer = StringWriter()
            val jsonGenerator = objectMapper.createGenerator(writer)

            jsonGenerator.writeStartObject()
            jsonGenerator.writeStringField(
                "__type",
                "dk.cachet.carp.data.infrastructure.DataStreamServiceRequest.AppendToDataStreams",
            )
            jsonGenerator.writeStringField("apiVersion", "1.1")
            jsonGenerator.writeStringField("studyDeploymentId", UUID.randomUUID().toString())

            jsonGenerator.writeArrayFieldStart("batch")
            repeat(Random.nextInt(1, 5)) { // Generate 1 to 5 batch objects
                jsonGenerator.writeStartObject()

                jsonGenerator.writeObjectFieldStart("dataStream")
                jsonGenerator.writeStringField("studyDeploymentId", UUID.randomUUID().toString())
                jsonGenerator.writeStringField("deviceRoleName", "Primary Phone")
                jsonGenerator.writeStringField("dataType", "dk.cachet.carp.heartbeat")
                jsonGenerator.writeEndObject()

                jsonGenerator.writeNumberField("firstSequenceId", Random.nextInt(Int.MAX_VALUE))

                jsonGenerator.writeArrayFieldStart("measurements")
                jsonGenerator.writeStartObject()
                jsonGenerator.writeNumberField("sensorStartTime", Random.nextLong())
                jsonGenerator.writeObjectFieldStart("data")
                jsonGenerator.writeStringField("__type", "dk.cachet.carp.heartbeat")
                jsonGenerator.writeNumberField("period", Random.nextInt())
                jsonGenerator.writeStringField("deviceType", "dk.cachet.carp.common.application.devices.Smartphone")
                jsonGenerator.writeStringField("deviceRoleName", "Primary Phone")
                jsonGenerator.writeEndObject()
                jsonGenerator.writeEndObject()
                jsonGenerator.writeEndArray()

                jsonGenerator.writeArrayFieldStart("triggerIds")
                jsonGenerator.writeNumber(Random.nextInt())
                jsonGenerator.writeEndArray()

                jsonGenerator.writeEndObject()
            }
            jsonGenerator.writeEndArray()

            jsonGenerator.writeEndObject()

            jsonGenerator.flush()

            return writer.toString()
        }
    }
}
