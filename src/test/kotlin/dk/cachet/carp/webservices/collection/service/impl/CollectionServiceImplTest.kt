package dk.cachet.carp.webservices.collection.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService

import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import io.mockk.*
import org.junit.jupiter.api.Nested
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authentication.domain.Account
import java.util.*
import kotlin.test.*

class CollectionServiceImplTest {
    private val collectionRepository: CollectionRepository = mockk()
    private val accountService: AccountService = mockk()
    private val authenticationService: AuthenticationService = mockk()
    private val objectMapper: ObjectMapper = mockk()
    private val validationMessages: MessageBase = mockk()

    @Nested
    inner class Delete {
        @Test
        fun `collection is deleted and relevant side tasks are executed`() {
            val mockStudyId = "123"
            val mockId = 1
            val mockCollection = mockk<Collection>(relaxed = true)
            val mockAccountIdentity = mockk<AccountIdentity>()
            every { collectionRepository.findCollectionByStudyIdAndId(mockStudyId, mockId) } returns Optional.of(
                mockCollection
            )
            every { collectionRepository.delete(mockCollection) } just Runs
            every { authenticationService.getCarpIdentity() } returns mockAccountIdentity
            coEvery {
                accountService.revoke(
                    mockAccountIdentity,
                    setOf(Claim.CollectionOwner(mockCollection.id))
                )
            } returns mockk<Account>()
            val sut = CollectionServiceImpl(
                collectionRepository,
                accountService,
                authenticationService,
                validationMessages,
                objectMapper
            )

            sut.delete(mockStudyId, mockId)

            verify(exactly = 1) { collectionRepository.delete(mockCollection) }
            verify(exactly = 1) { authenticationService.getCarpIdentity() }
            coVerify(exactly = 1) { accountService.revoke(mockAccountIdentity, setOf(Claim.CollectionOwner(mockCollection.id))) }
        }

        @Test
        fun `collection should not be deleted and relevant side tasks not executed if collection is not found`() {
            val mockStudyId = "123"
            val mockId = 1
            every { collectionRepository.findCollectionByStudyIdAndId(mockStudyId, mockId) } returns Optional.empty()
            every { authenticationService.getCarpIdentity() } returns mockk<AccountIdentity>()
            every { collectionRepository.delete(ofType<Collection>()) } just Runs
            coEvery {
                accountService.revoke(
                    any(),
                    any()
                )
            } returns mockk<Account>()

            every {
                validationMessages.get(
                    "collection.studyId-and-collectionId.not_found",
                    mockStudyId,
                    mockId
                )
            } returns "Collection not found"
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            assertFailsWith(ResourceNotFoundException::class) {
                sut.delete(mockStudyId, mockId)
            }

            verify(exactly = 0) { collectionRepository.delete(ofType<Collection>()) }
            verify(exactly = 0) { authenticationService.getCarpIdentity() }
            coVerify(exactly = 0) { accountService.revoke(any(), any()) }
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `collection is updated and returned`() {
            val mockStudyId = "123"
            val mockId = 1
            val updateRequest = CollectionUpdateRequestDto(name = "dsa")
            val collection = Collection(name = "old")
            every { collectionRepository.findCollectionByStudyIdAndId(mockStudyId, mockId) } returns Optional.of(
                collection
            )
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            val result = sut.update(mockStudyId, mockId, updateRequest)

            assertEquals("dsa", result.name)
        }

        @Test
        fun `collection is not updated if not found`() {
            val mockStudyId = "123"
            val mockId = 1
            val updateRequest = CollectionUpdateRequestDto(name = "dsa")
            every { collectionRepository.findCollectionByStudyIdAndId(mockStudyId, mockId) } returns Optional.empty()
            every {
                validationMessages.get(
                    "collection.studyId-and-collectionId.not_found",
                    mockStudyId,
                    mockId
                )
            } returns "Collection not found"
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            assertFailsWith(ResourceNotFoundException::class) {
                sut.update(mockStudyId, mockId, updateRequest)
            }
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `fun collection is created and returned`() {
            val mockStudyId = "123"
            val mockDeploymentId = "321"
            val mockRequest = CollectionCreateRequestDto(name = "dsa")
            val mockCollection = Collection().apply {
                name = mockRequest.name
                studyId = mockStudyId
                studyDeploymentId = mockDeploymentId
            }
            val mockAccountIdentity = mockk<AccountIdentity>()
            every { collectionRepository.findByStudyDeploymentIdAndName(mockDeploymentId, mockCollection.name) } returns Optional.empty()
            every { collectionRepository.save(ofType<Collection>()) } returns mockCollection
            every { authenticationService.getCarpIdentity() } returns mockAccountIdentity
            coEvery {
                accountService.grant(
                    mockAccountIdentity,
                    setOf(Claim.CollectionOwner(mockCollection.id))
                )
            } returns mockk<Account>()
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            val result = sut.create(mockRequest, mockStudyId, mockDeploymentId)

            assertEquals("dsa", result.name)
            assertEquals(mockStudyId, result.studyId)
            assertEquals(mockDeploymentId, result.studyDeploymentId)

            verify { collectionRepository.save(any()) }
            coVerify { accountService.grant(mockAccountIdentity, setOf(Claim.CollectionOwner(mockCollection.id))) }
        }

        //TODO add more from here
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
            assertEquals(mockCollection, result)
        }

        @Test
        fun `exception is thrown if collection is not present in database`() {
            val mockStudyId = "123"
            val mockId = 1
            every { collectionRepository.findCollectionByStudyIdAndId(mockStudyId, mockId) } returns Optional.empty()
            every {
                validationMessages.get(
                    "collection.studyId-and-collectionId.not_found",
                    mockStudyId,
                    mockId
                )
            } returns "Collection not found"
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            assertFailsWith(ResourceNotFoundException::class) {
                sut.getCollectionByStudyIdAndId(mockStudyId, mockId)
            }
        }
    }

    @Nested
    inner class GetCollectionByStudyIdAndByName {
        @Test
        fun `collection is returned if present in database`() {
            val mockStudyId = "123"
            val mockName = "name"
            val mockCollection = mockk<Collection>(relaxed = true)
            every { collectionRepository.findCollectionByStudyIdAndName(mockStudyId, mockName) } returns Optional.of(
                mockCollection
            )
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            val result = sut.getCollectionByStudyIdAndByName(mockStudyId, mockName)

            assertEquals(mockCollection, result)
        }

        @Test
        fun `exception is thrown if collection is not present in database`() {
            val mockStudyId = "123"
            val mockName = "name"
            every {
                collectionRepository.findCollectionByStudyIdAndName(
                    mockStudyId,
                    mockName
                )
            } returns Optional.empty()
            every {
                validationMessages.get(
                    "collection.studyId-and-collectionName.not_found",
                    mockStudyId,
                    mockName
                )
            } returns "Collection not found"
            val sut = CollectionServiceImpl(
                collectionRepository, accountService, authenticationService, validationMessages, objectMapper
            )

            assertFailsWith(ResourceNotFoundException::class) {
                sut.getCollectionByStudyIdAndByName(mockStudyId, mockName)
            }
        }
    }

    @Nested
    inner class GetAll {

    }

    @Nested
    inner class GetAllByStudyIdAndDeploymentId {

    }
}