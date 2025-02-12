package dk.cachet.carp.webservices.file.util
import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.walk

@Component
class FileUtil(
    private val filePermission: FilePermissionUtil,
    environment: Environment,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    // Storage directory
    final val storageDirectory: Path = Paths.get(convertStoragePath(environment.getProperty("storage.directory")!!))
    final val filePath: Path = Paths.get(FileUtils.getUserDirectoryPath() + storageDirectory)

    fun resolveFileStorage(fileName: String): Path {
        val rootFolder: Path? = Paths.get(filePath.toString()).toAbsolutePath().normalize()
        return storageDirectory.resolve(removeRootPrefix(rootFolder.toString()) + "/" + fileName)
    }

    fun resolveFileStoragePathForFilenameAndRelativePath(
        fileName: String,
        relativePath: Path,
    ): Path {
        val rootFolder: Path? = Paths.get(filePath.toString()).toAbsolutePath().normalize()
        val path =
            storageDirectory.resolve(
                removeRootPrefix(rootFolder.toString()) + "/" + relativePath + "/" + fileName,
            )
        isDirectoryOrElseCreate(path.parent)

        return path
    }

    fun isDirectoryOrElseCreate(storagePath: Path?): Path {
        val path: Path = Paths.get(removeRootPrefix(storagePath.toString()))
        if (!exists(path)) {
            createDirectories(path) ?: throw FileStorageException("Directories cannot be created.")
        }
        return path
    }

    fun setDefaultStoragePermissions(directory: Path?): Boolean {
        val retrieveDirectoryPermissions = directory?.let { filePermission.getPermissionsFor(it) }
        if (!retrieveDirectoryPermissions.equals("rwxr-xr-x")) {
            directory?.toFile()?.let { filePermission.setPermissionsFor(it, "rwxrwxrwx", true) }
        }
        if (retrieveDirectoryPermissions.equals("rwxr-xr-x")) {
            LOGGER.info("Directory $directory permissions are set to TRUE")
            return true
        }
        LOGGER.warn("Directory $directory permissions could not be determined.")
        return false
    }

    fun isDirectory(path: Path?): Boolean {
        return Files.isDirectory(path)
    }

    fun isWritable(path: Path?): Boolean {
        return Files.isWritable(path)
    }

    enum class OS { WINDOWS, LINUX, MACOSX, UNKNOWN; }

    fun getRunningOS(): OS {
        val os = System.getProperty("os.name")
        return when {
            os.startsWith("WIN") -> {
                OS.WINDOWS
            }

            os.startsWith("LIN") || os.contains("NUX") || os.contains("AI|X") -> {
                OS.LINUX
            }

            os.startsWith("MAC") -> {
                OS.MACOSX
            }

            else -> OS.UNKNOWN
        }
    }

    fun deleteFile(filePath: Path) {
        if (!filePath.toFile().exists()) {
            LOGGER.info("Request file system resource to delete does not exist! File delete operation aborted.")
            return
        }
        if (!filePath.toFile().isFile) {
            LOGGER.info("Request file system resource to delete is not a file! File delete operation aborted.")
            return
        }

        delete(filePath)
        LOGGER.info("File with name ${filePath.fileName} is deleted.")
    }

    @OptIn(ExperimentalPathApi::class)
    fun zipDirectory(
        dirPath: Path,
        zipPath: Path,
    ) {
        ZipOutputStream(newOutputStream(zipPath)).use { zipStream ->
            dirPath.walk().forEach { path ->
                val zipEntry = ZipEntry(dirPath.relativize(path).toString())
                try {
                    zipStream.putNextEntry(zipEntry)
                    copy(path, zipStream)
                    zipStream.closeEntry()
                } catch (e: IOException) {
                    LOGGER.error("An error occurred while zipping the file ${path.fileName}: ${e.message}")
                }
            }
        }

        LOGGER.info("Directory ${dirPath.fileName} is zipped and is available as $zipPath.")
    }

    fun removeRootPrefix(absolutePath: String?): String {
        val path = absolutePath.toString().replace("/root", "")
        LOGGER.info("The absolute path: $path")
        return path
    }

    private fun convertStoragePath(storagePath: String): String {
        if (getRunningOS() == OS.WINDOWS) {
            return storagePath.replace("/", "\\")
        }
        return storagePath
    }
}
