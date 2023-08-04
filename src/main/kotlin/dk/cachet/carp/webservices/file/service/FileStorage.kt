package dk.cachet.carp.webservices.file.service

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

/**
 * The Interface [FileStorage].
 * The [FileStorage] creates an interface for handling storage requests.
 */
interface FileStorage
{
    /** The [store] interface for storing files into the storage. */
    fun store(file: MultipartFile): String

    /** The [getFile] interface for download the file from storage. */
    fun getFile(fileName: String): Resource

    /** The [deleteFile] interface for deleting the file from storage. */
    fun deleteFile(filename: String): Boolean

    /** The [getResource] interface for export the file to storage. */
    fun getResource(fileName: String): Resource
}