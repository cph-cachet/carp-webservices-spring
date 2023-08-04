package dk.cachet.carp.webservices.file.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions

/**
 * The Class [FilePermissionUtil].
 * The [FilePermissionUtil] implements the functionalities to validate/set the main file storage with the read/write permissions.
 */
@Component
class FilePermissionUtil
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [getPermissions] method gets the permissions of the directory.
     *
     * @param path The [path] of the directory.
     * @return The directory permission.
     */
    @Throws(IOException::class)
    fun getPermissions(path: Path?): String?
    {
        val fileAttributeView = Files.getFileAttributeView(path, PosixFileAttributeView::class.java)
        val readAttributes = fileAttributeView.readAttributes()
        val permissions = readAttributes.permissions()

        return PosixFilePermissions.toString(permissions)
    }

    /**
     * The [setPermissions] sets the directory permissions.
     *
     * @param filePath The [filePath] to set the directory.
     * @param permissionCode The [permissionCode] of the directory.
     * @param recursive The [recursive] address of the directory.
     * @return The directory permission code.
     */
    @Throws(IOException::class)
    fun setPermissions(filePath: File, permissionCode: String?, recursive: Boolean): String?
    {
        val fileAttributeView = Files.getFileAttributeView(filePath.toPath(), PosixFileAttributeView::class.java)
        fileAttributeView.setPermissions(PosixFilePermissions.fromString(permissionCode))
        if(filePath.isDirectory && recursive && filePath.listFiles() != null)
        {
            for(f in filePath.listFiles())
            {
                setPermissions(f, permissionCode, true)
            }
        }
        LOGGER.info("Permission code set: {}", permissionCode)
        return permissionCode
    }
}