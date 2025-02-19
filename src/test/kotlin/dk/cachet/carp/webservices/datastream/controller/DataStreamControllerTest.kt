package dk.cachet.carp.webservices.datastream.controller

import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.test.*
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.datastream.service.impl.compressData
import kotlinx.coroutines.test.runTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class DataStreamControllerTest {
    private val dataStreamService: DataStreamService = mockk()
    val dss = mockk<dk.cachet.carp.data.application.DataStreamService>()
    val core = DataStreamServiceDecorator(dss) { command -> command }

    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(DataStreamController(dataStreamService)).build()
        coEvery { dataStreamService.core } returns core
    }

    @Nested
    inner class Invoke {
        val url_path = "/api/data-stream-service"

        @Test
        fun `should invoke`() {
            runTest {
                val mockUuids = setOf(UUID.randomUUID())
                val rpcRequest = DataStreamServiceRequest.CloseDataStreams(mockUuids)
                val serializedRequest = WS_JSON.encodeToString(DataStreamServiceRequest.Serializer, rpcRequest)

                coEvery { core.closeDataStreams(any()) } returns Unit

                mockMvc.perform(
                    post(url_path).contentType(MediaType.APPLICATION_JSON).content(serializedRequest),
                ).andExpect(status().isOk)

                coVerify { core.closeDataStreams(mockUuids) }
            }
        }
    }

    @Nested
    inner class HandleCompressedData {
        val url_path = "/api/data-stream-service-zip"

        @Test
        fun `should invoke`() {
            runTest {
                val mockUuids = setOf(UUID.randomUUID())
                val rpcRequest = DataStreamServiceRequest.CloseDataStreams(mockUuids)
                val serializedRequest = WS_JSON.encodeToString(DataStreamServiceRequest.Serializer, rpcRequest)
                val compressedData = compressData(serializedRequest)

                coEvery { core.closeDataStreams(any()) } returns Unit

                mockMvc.perform(
                    post(url_path).contentType(MediaType.APPLICATION_OCTET_STREAM).content(compressedData),
                ).andExpect(status().isOk)

                coVerify { core.closeDataStreams(mockUuids) }
            }
        }
    }
}
