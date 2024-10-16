package dk.cachet.carp.webservices.collection.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.service.CollectionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.BeforeTest
import kotlin.test.Test


class CollectionControllerTest {
    private val collectionService: CollectionService = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(CollectionController(collectionService)).build()
    }

    @Nested
    inner class Create {
        private val randomId = UUID.randomUUID().stringRepresentation
        private val url = "/api/studies/${randomId}/collections/"

        @Test
        fun `should return 400 if request body is invalid`() = runTest {
            val collectionCreateRequestDto = CollectionCreateRequestDto("")

            mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(collectionCreateRequestDto)),
            ).andExpect(status().isBadRequest)
        }

        @Test
        fun `should succeed if request is valid`() = runTest {
            val collectionCreateRequestDto = CollectionCreateRequestDto("someName", "someDeploymentId")
            val collectionMock: Collection = mockk(relaxed = true)
            every { collectionService.create(any(), any(), any()) } returns collectionMock

            mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(collectionCreateRequestDto)),
            ).andExpect(status().isCreated)

            verify { collectionService.create(collectionCreateRequestDto, randomId, "someDeploymentId") }
        }

    }

    @Nested
    inner class GetByStudyIdAndCollectionId {

    }

    @Nested
    inner class GetByStudyIdAndCollectionName {

    }

    @Nested
    inner class GetAll() {

    }

    @Nested
    inner class GetByStudyIdAndDeploymentId() {

    }

    @Nested
    inner class Delete() {

    }

    @Nested
    inner class Update() {

    }
}