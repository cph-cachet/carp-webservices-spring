package dk.cachet.carp.webservices.datastream.service.core

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.infrastructure.test.StubDataPoint
import dk.cachet.carp.webservices.datastream.repository.DataStreamConfigurationRepository
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
}
