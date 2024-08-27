package dk.cachet.carp.webservices.datastream.service.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.webservices.datastream.domain.DataStreamConfiguration
import dk.cachet.carp.webservices.datastream.repository.DataStreamConfigurationRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.createStubSequence
import dk.cachet.carp.webservices.datastream.service.impl.MutableDataStreamBatchDecorator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFailsWith

class CoreDataStreamServiceTest {
    @Nested
    inner class AppendToDataStreams {
        @Test
        fun `should throw if there are incorrect study deployment IDs in any of the batches`() =
            runTest {
                val incorrectId = UUID.randomUUID()
                val correctId = UUID.randomUUID()
                val batch = MutableDataStreamBatchDecorator()
                batch.appendSequence(createStubSequence(correctId, 0, StubDataPoint()))
                batch.appendSequence(createStubSequence(incorrectId, 0, StubDataPoint()))

                val sut =
                    CoreDataStreamService(
                        mockk(),
                        mockk(),
                        mockk(),
                        mockk(),
                    )

                assertThrows<IllegalArgumentException> {
                    sut.appendToDataStreams(correctId, batch)
                }
            }

        @Test
        fun `should throw if study deployment ID is not in configuration`() =
            runTest {
                val dataStreamConfigurationRepository = mockk<DataStreamConfigurationRepository>()
                val studyDeploymentId = UUID.randomUUID()

                val batch = MutableDataStreamBatchDecorator()
                batch.appendSequence(createStubSequence(studyDeploymentId, 0, StubDataPoint()))

                coEvery {
                    dataStreamConfigurationRepository.findById(studyDeploymentId.stringRepresentation)
                } returns Optional.empty()

                val sut =
                    CoreDataStreamService(
                        dataStreamConfigurationRepository,
                        mockk(),
                        mockk(),
                        mockk(),
                    )

                assertFailsWith<IllegalArgumentException> {
                    sut.appendToDataStreams(studyDeploymentId, batch)
                }

                coVerify(exactly = 1) {
                    dataStreamConfigurationRepository.findById(studyDeploymentId.stringRepresentation)
                }
            }
    }

    @Nested
    inner class GetDataStream {
        private val configRepository = mockk<DataStreamConfigurationRepository>()
        private val dataStreamIdRepository = mockk<DataStreamIdRepository>()
        private val dataStreamSequenceRepository = mockk<DataStreamSequenceRepository>()
        private val objectMapper = mockk<ObjectMapper>()

        private val sut =
            CoreDataStreamService(
                configRepository,
                dataStreamIdRepository,
                dataStreamSequenceRepository,
                objectMapper,
            )

        @Test
        fun `should throw IllegalArgumentException when fromSequenceId is negative`() =
            runTest {
                val dataStreamId =
                    DataStreamId(
                        UUID.randomUUID(),
                        "deviceRole",
                        DataType("namespace", "name"),
                    )

                assertThrows<IllegalArgumentException> {
                    sut.getDataStream(dataStreamId, -1, null)
                }
            }

        @Test
        fun `should throw IllegalArgumentException when toSequenceIdInclusive is smaller than fromSequenceId`() =
            runTest {
                val dataStreamId =
                    DataStreamId(
                        UUID.randomUUID(),
                        "deviceRole",
                        DataType("namespace", "name"),
                    )

                assertThrows<IllegalArgumentException> {
                    sut.getDataStream(dataStreamId, 10, 5)
                }
            }

        @Test
        fun `should throw IllegalArgumentException when no configuration is found for the given studyDeploymentId`() =
            runTest {
                val dataStreamId =
                    DataStreamId(
                        UUID.randomUUID(),
                        "deviceRole",
                        DataType("namespace", "name"),
                    )
                coEvery {
                    configRepository.findById(dataStreamId.studyDeploymentId.stringRepresentation)
                } returns Optional.empty()

                assertThrows<IllegalArgumentException> {
                    sut.getDataStream(dataStreamId, 0, null)
                }
            }

        @Test
        fun `should throw IllegalArgumentException when dataStream is not configured for the given studyDeploymentId`() =
            runTest {
                val dataStreamId =
                    DataStreamId(
                        UUID.randomUUID(),
                        "deviceRole",
                        DataType("namespace", "name"),
                    )
                val config = mockk<DataStreamConfiguration>()
                val configNode = mockk<JsonNode>()
                coEvery {
                    configRepository.findById(dataStreamId.studyDeploymentId.stringRepresentation)
                } returns Optional.of(config)
                coEvery { config.config } returns configNode
                val coreConfig = mockk<DataStreamsConfiguration>()
                coEvery { objectMapper.treeToValue(configNode, DataStreamsConfiguration::class.java) } returns coreConfig
                coEvery { coreConfig.expectedDataStreamIds } returns emptySet()

                assertThrows<IllegalArgumentException> {
                    sut.getDataStream(dataStreamId, 0, null)
                }
            }
    }
}
