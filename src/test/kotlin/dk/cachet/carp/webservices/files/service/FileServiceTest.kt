package dk.cachet.carp.webservices.files.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
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
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.StringUtils
import java.io.IOException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FileServiceTest {
    private val fileRepository: FileRepository = mockk()
    private val fileStorage: FileStorage = mockk()
    private val messageBase: MessageBase = mockk()
    private val s3Client: AmazonS3 = mockk()
    private val authenticationService: AuthenticationService = mockk()
    private val authorizationService: AuthorizationService = mockk()

    private val s3SpaceBucket = "s3://bucket"
    private val s3SpaceEndpoint = "https://why-is-aamir-written-with-two-As.com"

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
            val slot = slot<PutObjectRequest>()
            every { s3Client.putObject(capture(slot)) } returns mockk()

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

            verify(exactly = 0) { s3Client.deleteObject(any()) }
        }

        @Test
        fun `if url is valid use its uri key to delete s3 object`() {
            val slot = slot<DeleteObjectRequest>()
            every { s3Client.deleteObject(capture(slot)) } returns mockk()

            val expectedKey = "filename.png"
            val url = "$s3SpaceEndpoint/$s3SpaceBucket/filename.png"
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

            verify(exactly = 1) { s3Client.deleteObject(any()) }
            val actualKey = slot.captured.key
            assertEquals(expectedKey, actualKey)
        }
    }

    @Nested
    inner class DeleteAllOlderThan {
        @Test
        fun `deletes older than 7 days`() {
            val fileMock1 =
                mockk<File> {
                    every { id } returns 1
                    every { fileName } returns "file1"
                    every { relativePath } returns "foo/bar/baz"
                }
            val fileMock2 =
                mockk<File> {
                    every { id } returns 2
                    every { fileName } returns "file2"
                    every { relativePath } returns "foo/bar/qux"
                }
            every { fileRepository.getAllByUpdatedAtIsBefore(any()) } returns mutableListOf(fileMock1, fileMock2)
            every { fileRepository.delete(any<File>()) } just runs
            every { fileStorage.deleteFileAtPath(any(), any()) } returns true
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
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000

            sut.deleteAllOlderThan(7)

            verify { fileRepository.getAllByUpdatedAtIsBefore(java.time.Instant.ofEpochMilli(sevenDaysAgo)) }
            verify { fileRepository.delete(fileMock1) }
            verify { fileRepository.delete(fileMock2) }
            verify { fileStorage.deleteFileAtPath("file1", java.nio.file.Path.of("foo", "bar", "baz")) }
            verify { fileStorage.deleteFileAtPath("file2", java.nio.file.Path.of("foo", "bar", "qux")) }
        }

        @Test
        fun `should keep deleting even if one of the files could not be deleted from storage`() {
            val fileMock1 =
                mockk<File> {
                    every { id } returns 1
                    every { fileName } returns "file1"
                    every { relativePath } returns "foo/bar/baz"
                }
            val fileMock2 =
                mockk<File> {
                    every { id } returns 2
                    every { fileName } returns "file2"
                    every { relativePath } returns "foo/bar/qux"
                }
            every { fileRepository.getAllByUpdatedAtIsBefore(any()) } returns mutableListOf(fileMock1, fileMock2)
            every { fileRepository.delete(any<File>()) } just runs
            every {
                fileStorage.deleteFileAtPath(
                    "file1",
                    java.nio.file.Path.of("foo", "bar", "baz"),
                )
            } returns true
            every {
                fileStorage.deleteFileAtPath(
                    "file2",
                    java.nio.file.Path.of("foo", "bar", "qux"),
                )
            } throws IOException(":(")
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
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000

            sut.deleteAllOlderThan(7)

            verify { fileRepository.getAllByUpdatedAtIsBefore(any()) }
            verify { fileRepository.delete(fileMock1) }
            verify { fileRepository.delete(fileMock2) }
            verify { fileStorage.deleteFileAtPath("file1", java.nio.file.Path.of("foo", "bar", "baz")) }
            verify { fileStorage.deleteFileAtPath("file2", java.nio.file.Path.of("foo", "bar", "qux")) }
            assertThrows<IOException> {
                fileStorage.deleteFileAtPath("file2", java.nio.file.Path.of("foo", "bar", "qux"))
            }

            verify {
                fileRepository.getAllByUpdatedAtIsBefore(
                    match {
                        val tolerance = 5000
                        Math.abs(it.toEpochMilli() - sevenDaysAgo) <= tolerance
                    },
                )
            }
        }
    }
}
