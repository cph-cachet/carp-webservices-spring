package dk.cachet.carp.webservices.collection.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import io.mockk.mockk

import org.junit.jupiter.api.Assertions.*
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto
import dk.cachet.carp.webservices.collection.service.CollectionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class CollectionServiceImplTest {
    private val collectionRepository: CollectionRepository = mockk()
    private val accountService: AccountService = mockk()
    private val authenticationService: AuthenticationService = mockk()
    private val objectMapper: ObjectMapper = mockk()
    private val validationMessages: MessageBase = mockk()

    @Nested
    inner class Delete {
//        @Test
//        fun `collection is deleted and relevant side tasks are executed`() {
//
//
//            val sut = CollectionServiceImpl(
//                collectionRepository,
//                accountService,
//                authenticationService,
//                validationMessages,
//                objectMapper
//            )
//        }

    }

    @Nested
    inner class Update {

    }

    @Nested
    inner class Create {

    }

    @Nested
    inner class GetCollectionByStudyIdAndId {
        @Test
        fun `collection is returned if present in database`() {
            val mockStudyId = "123"
            val mockId = 1
            val mockCollection = mockk<Collection>(relaxed = true)
            every { collectionRepository.findCollectionByStudyIdAndId(mockStudyId, mockId) } returns Optional.of(
                mockCollection
            )

            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            val result = sut.getCollectionByStudyIdAndId(mockStudyId, mockId)

            assertEquals(mockCollection, objectMapper.writeValueAsString(result))
        }

    }

    @Nested
    inner class GetCollectionByStudyIdAndByName {

    }

    @Nested
    inner class GetAll {

    }

    @Nested
    inner class GetAllByStudyIdAndDeploymentId {

    }
}