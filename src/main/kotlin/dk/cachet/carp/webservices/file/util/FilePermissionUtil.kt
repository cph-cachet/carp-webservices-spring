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

@Component
class FilePermissionUtil {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    @Throws(IOException::class)
    fun getPermissionsFor(path: Path?): String? {
        val fileAttributeView = path?.let { Files.getFileAttributeView(it, PosixFileAttributeView::class.java) }
        val readAttributes = fileAttributeView?.readAttributes()
        val permissions = readAttributes?.permissions()

        return PosixFilePermissions.toString(permissions)
    }

    @Throws(IOException::class)
    fun setPermissionsFor(
        file: File,
        permissionCode: String?,
        recursive: Boolean,
    ): String? {
        val fileAttributeView = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView::class.java)
        fileAttributeView.setPermissions(PosixFilePermissions.fromString(permissionCode))
        if (file.isDirectory && recursive && file.listFiles() != null) {
            for (f in file.listFiles()) {
                setPermissionsFor(f, permissionCode, true)
            }
        }
        LOGGER.info("Permission code set: {}", permissionCode)
        return permissionCode
    }
}
