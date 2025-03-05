package dk.cachet.carp.webservices.files.service

import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.file.service.impl.FileServiceImpl
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileServiceTest {
    private val fileRepository: FileRepository = mockk()
    private val fileStorage: FileStorage = mockk()
    private val messageBase: MessageBase = mockk()
    private val s3Client: S3Client = mockk()
    private val authenticationService: AuthenticationService = mockk()
    private val authorizationService: AuthorizationService = mockk()

    private val s3SpaceBucket = "carp-dev"
    private val s3SpaceEndpoint = "https://fra1.digitaloceanspaces.com"

    @Nested
    inner class DeleteAllByStudyId {
        @Test
        fun `delete all files by study id`() {
            runTest {
                val studyId = dk.cachet.carp.common.application.UUID.randomUUID().stringRepresentation
                val relativePath1 = java.nio.file.Path.of("foo", "bar", "baz")
                val relativePath2 = java.nio.file.Path.of("foo", "bar", "qux")
                val file1 =
                    mockk<File> {
                        every { id } returns 1
                        every { fileName } returns "file1"
                        every { relativePath } returns relativePath1.toString()
                    }
                val file2 =
                    mockk<File> {
                        every { id } returns 2
                        every { fileName } returns "file2"
                        every { relativePath } returns relativePath2.toString()
                    }

                coEvery { fileRepository.findByStudyId(studyId) } returns listOf(file1, file2)

                val claim1 = Claim.FileOwner(file1.id)
                val claim2 = Claim.FileOwner(file2.id)

                coEvery { authorizationService.revokeClaimsFromAllAccounts(setOf(claim1, claim2)) } returns Unit
                coEvery { fileRepository.deleteById(1) } just runs
                coEvery { fileRepository.deleteById(2) } just runs
                coEvery { fileStorage.deleteFileAtPath("file1", relativePath1) } returns true
                coEvery { fileStorage.deleteFileAtPath("file2", relativePath2) } returns true

                val sut =
                    FileServiceImpl(
                        fileRepository,
                        fileStorage,
                        messageBase,
                        s3Client,
                        authenticationService,
                        s3SpaceBucket,
                        s3SpaceEndpoint,
                    )

                sut.deleteAllByStudyId(studyId)

                verify { fileRepository.findByStudyId(studyId) }
                coVerify(exactly = 0) { authorizationService.revokeClaimsFromAllAccounts(setOf(claim1, claim2)) }
                coVerify { fileRepository.deleteById(1) }
                coVerify { fileRepository.deleteById(2) }
                coVerify { fileStorage.deleteFileAtPath("file1", relativePath1) }
                coVerify { fileStorage.deleteFileAtPath("file2", relativePath2) }
            }
        }
    }

    @Nested
    inner class UploadImage {
        @Test
        fun `if file has no contentType, metadata contentType should be null`() {
            val putObjectRequestSlot = slot<PutObjectRequest>()
            every {
                s3Client.putObject(
                    capture(putObjectRequestSlot),
                    any<software.amazon.awssdk.core.sync.RequestBody>(),
                )
            } returns mockk()

            val multipartFile = MockMultipartFile("name", "originalName.png", null, byteArrayOf(0))
            val sut =
                FileServiceImpl(
                    fileRepository,
                    fileStorage,
                    messageBase,
                    s3Client,
                    authenticationService,
                    s3SpaceBucket,
                    s3SpaceEndpoint,
                )

            sut.uploadImage(multipartFile, "studyId")

            verify { s3Client.putObject(any<PutObjectRequest>(), any<software.amazon.awssdk.core.sync.RequestBody>()) }
            assertEquals(null, putObjectRequestSlot.captured.metadata()["Content-Type"])
        }

        @Test
        fun `uploaded image should have the same extension and a valid path and a valid uuid name`() {
            val putObjectRequestSlot = slot<PutObjectRequest>()
            every {
                s3Client.putObject(
                    capture(putObjectRequestSlot),
                    any<software.amazon.awssdk.core.sync.RequestBody>(),
                )
            } returns mockk()

            val filename = "originalName.png"
            val multipartFile = MockMultipartFile("name", filename, null, byteArrayOf(0))
            val sut =
                FileServiceImpl(
                    fileRepository,
                    fileStorage,
                    messageBase,
                    s3Client,
                    authenticationService,
                    s3SpaceBucket,
                    s3SpaceEndpoint,
                )

            sut.uploadImage(multipartFile, "123")

            verify(exactly = 1) {
                s3Client.putObject(
                    putObjectRequestSlot.captured,
                    any<software.amazon.awssdk.core.sync.RequestBody>(),
                )
            }
            val actualKey = putObjectRequestSlot.captured.key()
            assertTrue { actualKey.contains("studies/123/") }
            assertTrue { actualKey.contains(".png") }

            // fromString throws an exception if uuid is not valid
            UUID.fromString(actualKey.substringAfter("studies/123/").substringBefore(".png"))
        }
    }

    @Nested
    inner class DeleteImage {
        @Test
        fun `should delete image`() {
            every { s3Client.deleteObject(any<DeleteObjectRequest>()) } returns mockk()
            val url =
                "https://carp-dev.fra1.digitaloceanspaces.com/studies/c424c83d-9e33-4482-b8ff-5ecc095a6de2/" +
                        "59a66f4a-6bd4-4800-82f7-d89504672f9e.png"
            val sut =
                FileServiceImpl(
                    fileRepository,
                    fileStorage,
                    messageBase,
                    s3Client,
                    authenticationService,
                    s3SpaceBucket,
                    s3SpaceEndpoint,
                )

            sut.deleteImage(url)

            verify { s3Client.deleteObject(any<DeleteObjectRequest>()) }
        }
    }
}
