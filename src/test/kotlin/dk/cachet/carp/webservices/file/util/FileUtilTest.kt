package dk.cachet.carp.webservices.file.util

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.env.Environment
import kotlin.io.path.Path
import kotlin.test.assertEquals

class FileUtilTest {
    private val filePermission: FilePermissionUtil = mockk()
    private val environment: Environment = mockk()

    @Nested
    inner class ResolveFileStorage {
        @Test
        fun `should resolve file storage`() {
            runTest {
                val filename = "filename"

                every { environment.getProperty("storage.directory") } returns "/foo/bar/baz/"
                val sut = FileUtil(filePermission, environment)

                val result = sut.resolveFileStorage(filename)

                val expected = Path(FileUtils.getUserDirectoryPath() + "/foo/bar/baz/filename")
                assertEquals(expected, result)
            }
        }
    }

    @Nested
    inner class ResolveFileStoragePathForFilenameAndRelativePath {
        @Test
        fun `should resolve file storage path for filename and relative path`() {
            runTest {
                val filename = "filename"
                val relativePath = Path("/studies/study-1/")

                every { environment.getProperty("storage.directory") } returns "/foo/bar/baz/"
                val sut = FileUtil(filePermission, environment)

                val result = sut.resolveFileStoragePathForFilenameAndRelativePath(filename, relativePath)

                val expected = Path(FileUtils.getUserDirectoryPath() + "/foo/bar/baz/studies/study-1/filename")
                assertEquals(expected, result)
            }
        }
    }
}
