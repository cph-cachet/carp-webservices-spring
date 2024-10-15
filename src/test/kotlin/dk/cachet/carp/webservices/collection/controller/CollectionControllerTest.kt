package dk.cachet.carp.webservices.collection.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.account.domain.AccountRequest
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.service.CollectionService
import dk.cachet.carp.webservices.security.authorization.Role
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
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
        @Test
        fun `should return 400 if request body is invalid`() = runTest {
            val collectionCreateRequestDto = CollectionCreateRequestDto("")

            mockMvc.perform(
                post("/api/studies/1/collections")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(collectionCreateRequestDto)),
            ).andExpect(status().isBadRequest)
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