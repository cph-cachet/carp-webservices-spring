package dk.cachet.carp.webservices.files.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.file.service.impl.FileServiceImpl
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.StringUtils
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FileServiceTest {

    private val fileRepository: FileRepository = mockk()
    private val fileStorage: FileStorage = mockk()
    private val messageBase: MessageBase = mockk()
    private val s3Client: AmazonS3 = mockk()
    private val authenticationService: AuthenticationService = mockk()
    private val accountService: AccountService = mockk()

    private val s3SpaceBucket = "s3://bucket"
    private val s3SpaceEndpoint = "https://why-is-aamir-written-with-two-As.com"

    @Nested
    inner class UploadImage {
        @Test
        fun `if file has no contentType, metadata contentType should be null`() {
            val slot = slot<PutObjectRequest>()
            every { s3Client.putObject(capture(slot)) } returns mockk()

            val multipartFile = MockMultipartFile("name", "originalName.png", null, byteArrayOf(0))
            val sut = FileServiceImpl(
                fileRepository,
                fileStorage,
                messageBase,
                s3Client,
                authenticationService,
                accountService,
                s3SpaceBucket,
                s3SpaceEndpoint
            )

            sut.uploadImage(multipartFile)

            verify(exactly = 1) { s3Client.putObject(any()) }
            assertEquals(null, slot.captured.metadata.contentType)
        }

        @Test
        fun `uploaded image should have the same extension but a valid uuid as name`() {
            val slot = slot<PutObjectRequest>()
            every { s3Client.putObject(capture(slot)) } returns mockk()

            val expectedExtension = "png"
            val originalKey = "originalName.$expectedExtension"
            val multipartFile = MockMultipartFile("name", originalKey, null, byteArrayOf(0))
            val sut = FileServiceImpl(
                fileRepository,
                fileStorage,
                messageBase,
                s3Client,
                authenticationService,
                accountService,
                s3SpaceBucket,
                s3SpaceEndpoint
            )

            sut.uploadImage(multipartFile)

            verify(exactly = 1) { s3Client.putObject(slot.captured) }
            val actualKey = slot.captured.key
            val actualExtension = StringUtils.getFilenameExtension(actualKey)
            val actualFilename = actualKey.replace(".$actualExtension", "")
            assertNotEquals(originalKey, actualKey)
            assertEquals(expectedExtension, actualExtension)

            // fromString throws an exception if uuid is not valid
            UUID.fromString(actualFilename)
        }
    }

    @Nested
    inner class DeleteImage {
        @Test
        fun `if url is invalid, do nothing`() {
            every { s3Client.deleteObject(any()) } returns mockk()
            val url = "some not valid url"
            val sut = FileServiceImpl(
                fileRepository,
                fileStorage,
                messageBase,
                s3Client,
                authenticationService,
                accountService,
                s3SpaceBucket,
                s3SpaceEndpoint
            )

            sut.deleteImage(url)

            verify(exactly = 0) { s3Client.deleteObject(any()) }
        }

        @Test
        fun `if url is valid use its uri key to delete s3 object`() {
            val slot = slot<DeleteObjectRequest>()
            every { s3Client.deleteObject(capture(slot)) } returns mockk()

            val expectedKey = "filename.png"
            val url = "$s3SpaceEndpoint/$s3SpaceBucket/filename.png"
            val sut = FileServiceImpl(
                fileRepository,
                fileStorage,
                messageBase,
                s3Client,
                authenticationService,
                accountService,
                s3SpaceBucket,
                s3SpaceEndpoint
            )

            sut.deleteImage(url)

            verify(exactly = 1) { s3Client.deleteObject(any()) }
            val actualKey = slot.captured.key
            assertEquals(expectedKey, actualKey)
        }
    }
}