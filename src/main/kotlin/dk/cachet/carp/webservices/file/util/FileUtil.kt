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

/**
 * The Class [FileUtil].
 * The [FileUtil] implements the permission to the directory and returns true if
 *  - exists
 *  - is a directory
 *  - is writable.
 *  - and set the permissions on the directory if the directory exists and is not writable.
 */
@Component
class FileUtil(
        private val filePermission: FilePermissionUtil,
        environment: Environment
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    // Storage directory
    final val storageDirectory: Path = Paths.get(convertStoragePath(environment.getProperty("storage.directory")!!))
    final val filePath: Path = Paths.get(FileUtils.getUserDirectoryPath() + storageDirectory)

    /**
     * The [resolveFileStorage] method is used to resolve the storage path.
     *
     * @param fileName The [fileName] to resolve.
     * @return The resolved path storage.
     */
    fun resolveFileStorage(fileName: String): Path
    {
        val rootFolder: Path? = Paths.get(filePath.toString()).toAbsolutePath().normalize()
        return storageDirectory.resolve(removeRootPrefix(rootFolder.toString())+"/"+fileName)
    }

    /**
     * The [isDirectoryOrElseCreate] validates directory existence and returns true, otherwise false.
     *
     * @param [storagePath] The directory to validate. If does not exist, try creating directory with the given name.
     * @throws [FileStorageException] The FileStorageException when the directory is not a directory and cannot be created.
     * @return [Boolean] `true` if the directory exists, `false` otherwise.
     */
    fun isDirectoryOrElseCreate(storagePath: Path?): Path
    {
        val path: Path = Paths.get(removeRootPrefix(storagePath.toString()))
        if (!exists(path))
        {
            createDirectories(path) ?: throw FileStorageException("Directories cannot be created.")
        }
        return path
    }

    /**
     * The [setStoragePermission] set the directory permissions.
     *
     * @param directory The [directory] to set the permissions.
     * @return [Boolean] `true` if the directory permissions are set, otherwise `false`.
     */
    fun setStoragePermission(directory: Path?): Boolean
    {
        val retrieveDirectoryPermissions = directory?.let { filePermission.getPermissions(it) }
        if (!retrieveDirectoryPermissions.equals("rwxr-xr-x") )
        {
            directory?.toFile()?.let { filePermission.setPermissions(it, "rwxrwxrwx", true) }
        }
        if (retrieveDirectoryPermissions.equals("rwxr-xr-x"))
        {
            LOGGER.info("Directory $directory permissions are set to TRUE")
            return true
        }
        LOGGER.warn("Directory $directory permissions could not be determined.")
        return false
    }

    /**
     * The [isDirectory] validates the path and returns true if directory is a directory.
     *
     * @param [path] The [path] to validate.
     * @return [Boolean] `true` if the directory is a directory, `false` otherwise.
     */
    fun isDirectory(path: Path?): Boolean
    {
        return Files.isDirectory(path)
    }

    /**
     * The [exists] validates a directory and returns true if exists.
     *
     * @param [path] The path to validate.
     * @return [Boolean] `true` if the directory exists, `false` otherwise.
     */
    fun exists(path: Path?): Boolean
    {
        return Files.exists(path)
    }

    /**
     * The [isWritable] validates a directory and returns true if directory is writable.
     *
     * @param path The [path] to validate.
     * @return [Boolean] `true` if the directory is writable, `false` otherwise.
     */
    fun isWritable(path: Path?): Boolean
    {
        return Files.isWritable(path)
    }

    /**
     * The [convertStoragePath] function converts the storage path base on the operating system.
     *
     * @param storagePath The [storagePath] to convert.
     * @return The [storagePath] of the directory.
     */
    private fun convertStoragePath(storagePath: String): String
    {
        if (getRunningOS() == OS.WINDOWS)
        {
            return storagePath.replace("/", "\\")
        }
        return storagePath
    }

    // Enum class representing a operating system type.
    enum class OS { WINDOWS, LINUX, MACOSX, UNKNOWN; }

    /**
     * The [getRunningOS] function returns the operating system information.
     * @return [String] The operating system information.
     */
    fun getRunningOS(): OS
    {
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

    /**
     * Deletes a file.
     *
     * @param [filePath] [Path] of the file.
     */
    fun deleteFile(filePath: Path)
    {
        if (!filePath.toFile().exists())
        {
            LOGGER.info("Request file system resource to delete does not exist! File delete operation aborted.")
            return
        }
        if (!filePath.toFile().isFile)
        {
            LOGGER.info("Request file system resource to delete is not a file! File delete operation aborted.")
            return
        }

        delete(filePath)
        LOGGER.info("File with name ${filePath.fileName} is deleted.")
    }

    @OptIn(ExperimentalPathApi::class)
    fun zipDirectory( dirPath: Path, zipPath: Path )
    {
        ZipOutputStream( newOutputStream( zipPath ) ).use { zipStream ->
            dirPath.walk().forEach { path ->
                val zipEntry = ZipEntry( dirPath.relativize( path ).toString() )
                try
                {
                    zipStream.putNextEntry( zipEntry )
                    copy( path, zipStream )
                    zipStream.closeEntry()
                }
                catch ( e: IOException )
                {
                   LOGGER.error( "An error occurred while zipping the file ${path.fileName}: ${e.message}" )
                }
            }
        }

        LOGGER.info( "Directory ${dirPath.fileName} is zipped and is available as ${zipPath}." )
    }

    /**
     * The [removeRootPrefix] function is used to remove the root prefix.
     * @return The current file path.
     */
    fun removeRootPrefix(absolutePath: String?): String
    {
        val path = absolutePath.toString().replace("/root","")
        LOGGER.info("The absolute path: $path")
        return path
    }
}