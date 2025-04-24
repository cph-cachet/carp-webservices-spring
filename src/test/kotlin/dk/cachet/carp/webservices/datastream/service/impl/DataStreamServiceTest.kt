package dk.cachet.carp.webservices.datastream.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.core.CoreDataStreamService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
import org.junit.jupiter.api.assertThrows
import dk.cachet.carp.webservices.deployment.service.ParticipationService
import kotlinx.datetime.Instant


class DataStreamServiceTest {
    private val dataStreamIdRepository = mockk<DataStreamIdRepository>()
    private val dataStreamSequenceRepository = mockk<DataStreamSequenceRepository>()
    private val objectMapper = mockk<ObjectMapper>()
    private val participantRepository = mockk<ParticipantRepository>()
    private val participationService = mockk<ParticipationService>()
    val services: CoreServiceContainer =
        mockk<CoreServiceContainer> {
            every { dataStreamService } returns mockk<DataStreamServiceDecorator>()
        }


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
                val zipOutputStream = compressData(requestAsJson)

                val services = mockk<CoreServiceContainer>()
                val coreDataStreamService = mockk<CoreDataStreamService>()
                every { services.dataStreamService } returns DataStreamServiceDecorator(coreDataStreamService, mockk())

                assertDoesNotThrow {
                    withContext(Dispatchers.IO) {
                        decompressGzip(zipOutputStream)
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

                assertFailsWith<IOException> {
                    withContext(Dispatchers.IO) {
                        decompressGzip(mockFile.bytes)
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

    @Nested
    inner class GetDataStreamsSummary {
        @Test
        fun `throws exception if type is invalid`() {
            runTest {
                val sut = DataStreamService(
                    dataStreamIdRepository,
                    dataStreamSequenceRepository,
                    objectMapper,
                    participantRepository,
                    participationService,
                    services,
                )

                assertThrows<IllegalArgumentException> {
                    sut.getDataStreamsSummary(
                        studyId = UUID.randomUUID(),
                        deploymentId = UUID.randomUUID(),
                        participantId = UUID.randomUUID(),
                        scope = "invalidScope",
                        type = "invalidType",
                        from = Instant.fromEpochMilliseconds(0),
                        to = Instant.fromEpochMilliseconds(0),
                    )
                }
            }
        }
    }
}
