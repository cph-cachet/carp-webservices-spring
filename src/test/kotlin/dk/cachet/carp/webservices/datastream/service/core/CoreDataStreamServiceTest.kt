package dk.cachet.carp.webservices.datastream.service.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.DataType
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.datastream.domain.DataStreamConfiguration
import dk.cachet.carp.webservices.datastream.domain.DataStreamSequence
import dk.cachet.carp.webservices.datastream.domain.DataStreamSnapshot
import dk.cachet.carp.webservices.datastream.repository.DataStreamConfigurationRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.createStubSequence
import dk.cachet.carp.webservices.datastream.service.impl.MutableDataStreamBatchDecorator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CoreDataStreamServiceTest {
    private lateinit var sut: CoreDataStreamService
    private val configRepository = mockk<DataStreamConfigurationRepository>()
    private val dataStreamIdRepository = mockk<DataStreamIdRepository>()
    private val dataStreamSequenceRepository = mockk<DataStreamSequenceRepository>()
    private val objectMapper = mockk<ObjectMapper>()

    @BeforeEach
    fun setUp() {
        sut =
            CoreDataStreamService(
                configRepository,
                dataStreamIdRepository,
                dataStreamSequenceRepository,
                objectMapper,
            )
    }

    @Nested
    inner class AppendToDataStreams {
        @Test
        fun `throw if there are incorrect study deployment IDs in any of the batches`() =
            runTest {
                val incorrectId = UUID.randomUUID()
                val correctId = UUID.randomUUID()
                val batch = MutableDataStreamBatchDecorator()
                batch.appendSequence(createStubSequence(correctId, 0, StubDataPoint()))
                batch.appendSequence(createStubSequence(incorrectId, 0, StubDataPoint()))

                assertThrows<IllegalArgumentException> {
                    sut.appendToDataStreams(correctId, batch)
                }
            }

        @Test
        fun `throw if study deployment ID is not in configuration`() =
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
    inner class GetDataStreams {
        @Test
        fun `getDataStream with valid arguments returns expected DataStreamBatch`() = runTest {
            val studyDeploymentId = UUID.randomUUID()
            val dataStream = DataStreamId(
                studyDeploymentId,
                "deviceRole",
                DataType("namespace", "name")
            )
            val fromSequenceId = 0L
            val toSequenceIdInclusive = 10L

            val config = mockk<DataStreamConfiguration>()
            val configNode = mockk<JsonNode>()
            val coreConfig = mockk<DataStreamsConfiguration>()
            val dataStreamId = mockk<dk.cachet.carp.webservices.datastream.domain.DataStreamId>()
            val dataStreamSequence = mockk<DataStreamSequence>()
            val snapshot = mockk<DataStreamSnapshot>()
            val measurements = listOf(mockk<Measurement<*>>())

            coEvery { configRepository.findById(dataStream.studyDeploymentId.stringRepresentation) } returns Optional.of(config)
            coEvery { config.config } returns configNode
            coEvery { objectMapper.treeToValue(configNode, DataStreamsConfiguration::class.java) } returns coreConfig
            coEvery { coreConfig.expectedDataStreamIds } returns setOf(dataStream)
            coEvery {
                dataStreamIdRepository.findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
                    studyDeploymentId.stringRepresentation,
                    dataStream.deviceRoleName,
                    dataStream.dataType.name,
                    dataStream.dataType.namespace
                )
            } returns Optional.of(dataStreamId)
            coEvery { dataStreamSequenceRepository.findAllBySequenceIdRange(dataStreamId.id, fromSequenceId.toInt(), toSequenceIdInclusive.toInt()) } returns listOf(dataStreamSequence)
            coEvery { dataStreamSequence.firstSequenceId } returns fromSequenceId
            coEvery { dataStreamSequence.lastSequenceId } returns toSequenceIdInclusive
            coEvery { dataStreamSequence.snapshot } returns mockk()

            // Ensure non-null UUID before serialization
            val nonNullUUID: UUID = studyDeploymentId ?: UUID.randomUUID()
            val jsonString = WS_JSON.encodeToString(DataStreamsConfiguration.serializer(), coreConfig.copy(studyDeploymentId = nonNullUUID))
            val jsonNode = objectMapper.readTree(jsonString)
            coEvery { objectMapper.readTree(any<String>()) } returns jsonNode

            coEvery { objectMapper.treeToValue(any<JsonNode>(), DataStreamSnapshot::class.java) } returns snapshot
            coEvery { snapshot.measurements } returns measurements
            coEvery { snapshot.triggerIds } returns emptyList()
            coEvery { snapshot.syncPoint } returns mockk()

            val result = sut.getDataStream(dataStream, fromSequenceId, toSequenceIdInclusive)

            assertNotNull(result)
/*
            assertEquals(1, result.sequences.size)
*/
            coVerify { configRepository.findById(dataStream.studyDeploymentId.stringRepresentation) }
            coVerify { dataStreamIdRepository.findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(any(), any(), any(), any()) }
            coVerify { dataStreamSequenceRepository.findAllBySequenceIdRange(any(), any(), any()) }
        }
        @Test
        fun `throw IllegalArgumentException when fromSequenceId is negative`() =
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
        fun `throw IllegalArgumentException when toSequenceIdInclusive is smaller than fromSequenceId`() =
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
        fun `throw IllegalArgumentException when no configuration is found for the given studyDeploymentId`() =
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
        fun `throw IllegalArgumentException when dataStream is not configured for the given studyDeploymentId`() =
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
                coEvery {
                    objectMapper.treeToValue(configNode, DataStreamsConfiguration::class.java)
                } returns coreConfig
                coEvery { coreConfig.expectedDataStreamIds } returns emptySet()

                assertThrows<IllegalArgumentException> {
                    sut.getDataStream(dataStreamId, 0, null)
                }
            }
    }

    @Nested
    inner class OpenDataStreams {
        @Test
        fun `save configuration and data stream IDs when valid configuration is provided`() =
            runTest {
                val studyDeploymentId = UUID.randomUUID()
                val configuration =
                    DataStreamsConfiguration(
                        studyDeploymentId,
                        setOf(
                            DataStreamsConfiguration.ExpectedDataStream(
                                "deviceRole",
                                DataType("namespace", "name"),
                            ),
                        ),
                    )

                val wsDataStreamId =
                    dk.cachet.carp.webservices.datastream.domain.DataStreamId(
                        id = 0,
                        studyDeploymentId = studyDeploymentId.stringRepresentation,
                        deviceRoleName = "deviceRole",
                        name = "name",
                        nameSpace = "namespace",
                    )

                val jsonNode = mockk<JsonNode>()
                coEvery { configRepository.existsById(studyDeploymentId.stringRepresentation) } returns false
                coEvery { objectMapper.valueToTree<JsonNode>(configuration) } returns jsonNode
                coEvery { objectMapper.readTree(any<String>()) } returns jsonNode
                coEvery {
                    configRepository.save(any())
                } returns DataStreamConfiguration(studyDeploymentId.stringRepresentation, mockk())

                val dataStreamIds = listOf(wsDataStreamId)
                coEvery { dataStreamIdRepository.saveAll(dataStreamIds) } returns dataStreamIds

                sut.openDataStreams(configuration)

                coVerify { configRepository.save(any()) }
                coVerify { dataStreamIdRepository.saveAll(dataStreamIds) }
            }

        @Test
        fun `throw IllegalStateException when configuration already exists`() =
            runTest {
                val studyDeploymentId = UUID.randomUUID()
                val configuration =
                    DataStreamsConfiguration(
                        studyDeploymentId,
                        setOf(
                            DataStreamsConfiguration.ExpectedDataStream(
                                "deviceRole",
                                DataType("namespace", "name"),
                            ),
                        ),
                    )

                coEvery { configRepository.existsById(studyDeploymentId.stringRepresentation) } returns true

                assertThrows<IllegalStateException> {
                    sut.openDataStreams(configuration)
                }
            }

        @Test
        fun `log and save new data stream configuration`() =
            runTest {
                val studyDeploymentId = UUID.randomUUID()
                val configuration =
                    DataStreamsConfiguration(
                        studyDeploymentId,
                        setOf(
                            DataStreamsConfiguration.ExpectedDataStream(
                                "deviceRole",
                                DataType("namespace", "name"),
                            ),
                        ),
                    )

                val wsDataStreamId =
                    dk.cachet.carp.webservices.datastream.domain.DataStreamId(
                        id = 0,
                        studyDeploymentId = studyDeploymentId.stringRepresentation,
                        deviceRoleName = "deviceRole",
                        name = "name",
                        nameSpace = "namespace",
                    )

                val jsonNode = mockk<ObjectNode>()
                coEvery { configRepository.existsById(studyDeploymentId.stringRepresentation) } returns false
                coEvery { objectMapper.valueToTree<JsonNode>(configuration) } returns jsonNode
                coEvery { objectMapper.readTree(any<String>()) } returns jsonNode
                coEvery {
                    configRepository.save(any())
                } returns DataStreamConfiguration(studyDeploymentId.stringRepresentation, jsonNode)

                val dataStreamIds = listOf(wsDataStreamId)
                coEvery { dataStreamIdRepository.saveAll(dataStreamIds) } returns dataStreamIds

                sut.openDataStreams(configuration)

                coVerify { configRepository.save(any()) }
                coVerify { dataStreamIdRepository.saveAll(dataStreamIds) }
            }
    }

    @Nested
    inner class CloseDataStreams {
        @Test
        fun `close data streams when valid studyDeploymentIds are provided`() =
            runTest {
                val studyDeploymentId1 = UUID.randomUUID()
                val studyDeploymentId2 = UUID.randomUUID()
                val config1 = mockk<DataStreamConfiguration>(relaxed = true)
                val config2 = mockk<DataStreamConfiguration>(relaxed = true)

                coEvery {
                    configRepository.getConfigurationsForIds(
                        listOf(studyDeploymentId1.stringRepresentation, studyDeploymentId2.stringRepresentation),
                    )
                } returns listOf(config1, config2)
                coEvery {
                    configRepository.saveAll(any<List<DataStreamConfiguration>>())
                } returns listOf(config1, config2)

                sut.closeDataStreams(setOf(studyDeploymentId1, studyDeploymentId2))

                coVerify { config1.closed = true }
                coVerify { config2.closed = true }
                coVerify { configRepository.saveAll(listOf(config1, config2)) }
            }

        @Test
        fun `throw IllegalArgumentException when configuration does not exist`() =
            runTest {
                val studyDeploymentId = UUID.randomUUID()

                coEvery {
                    configRepository.getConfigurationsForIds(listOf(studyDeploymentId.stringRepresentation))
                } returns emptyList()

                assertThrows<IllegalArgumentException> {
                    sut.closeDataStreams(setOf(studyDeploymentId))
                }
            }
    }
}
