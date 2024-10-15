package dk.cachet.carp.webservices.collection.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.collection.service.CollectionService
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.BeforeTest

class CollectionControllerTest {
    private val collectionService: CollectionService = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(CollectionController(collectionService)).build()
    }

}