package dk.cachet.carp.webservices.file.service.impl

import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.file.util.FileUtil
import org.apache.commons.io.FilenameUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * The Class [FileStorageImpl].
 * The [FileStorageImpl] provides the implementation to download, upload, and retrieves files.
 */
@Service
class FileStorageImpl(
    private val fileUtil: FileUtil,
    private val validationMessages: MessageBase,
) : FileStorage {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [store] enables storing the files into the file storage.
     *
     * @param file The [file] to store into the file storage.
     * @throws FileStorageException when the file cannot be stored/written into the storage.
     * @return A [file] stored in the storage.
     */
    override fun store(file: MultipartFile): String {
        val fileName = generateFileName(file)
        val filePath = resolveFullPathForFilename(fileName)
        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            return fileName
        } catch (ex: IOException) {
            LOGGER.error("Failed to store the file ${file.originalFilename} into file storage. ", ex)
            throw FileStorageException(validationMessages.get("file.store.failed", file.originalFilename!!))
        }
    }

    /**
     * Similar to [store], but allows storing the file at a specific path relative to the base storage directory.
     * E.g. /Users/johnpork/home/carp/storage/local/studies/123 where /studies/123/ is the relativePathFromBase argument to method call.
     *
     * @param file The [file] to store into the file storage.
     * @throws FileStorageException when the file cannot be stored/written into the storage.
     * @return A [file] stored in the storage.
     */
    override fun storeAtPath(file: MultipartFile, relativePathFromBase: Path): String {
        val fileName = generateFileName(file)
        val filePath = fileUtil.resolveFileStoragePathForFilenameAndRelativePath(fileName, relativePathFromBase)

        Files.createDirectories(filePath.parent)

        try {
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            return fileName
        } catch (ex: IOException) {
            LOGGER.error("Failed to store the file ${file.originalFilename} into file storage. ", ex)
            throw FileStorageException(validationMessages.get("file.store.failed", file.originalFilename!!))
        }
    }

    /**
     * The function [getFile] retrieves the file with the given [fileName] parameter.
     *
     * @param fileName The [fileName] of the file to retrieve.
     * @throws FileStorageException when the file does not exist or is not readable.
     * @return The [Resource] of the file requested.
     */
    override fun getFile(fileName: String): Resource {
        try {
            val file = resolveFullPathForFilename(fileName)
            val resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            }

            throw ResourceNotFoundException(validationMessages.get("file.store.file.exists", fileName))
        } catch (ex: MalformedURLException) {
            LOGGER.error("Unable to resolve file location: $fileName", ex)
            throw BadRequestException(
                validationMessages.get("file.store.file.resolve", fileName, ex.message.toString()),
            )
        }
    }

    /**
     * The function [getFileAtPath] retrieves the file with the given [fileName], [relativePathFromBase] parameters.
     *
     * @param fileName The [fileName] of the file to retrieve.
     * @param relativePathFromBase The [relativePathFromBase] path of the file.
     * @throws FileStorageException when the file does not exist or is not readable.
     * @return The [Resource] of the file requested.
     */
    override fun getFileAtPath(fileName: String, relativePathFromBase: Path): Resource {
        try {
            val file = resolveFullPathForFilenameAndRelativePath(fileName, relativePathFromBase)
            val resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            }

            throw ResourceNotFoundException(validationMessages.get("file.store.file.exists", fileName))
        } catch (ex: MalformedURLException) {
            LOGGER.error("Unable to resolve file location: $fileName", ex)
            throw BadRequestException(
                validationMessages.get("file.store.file.resolve", fileName, ex.message.toString()),
            )
        }
    }

    /**
     * Delete the file from storage.
     *
     * @param filename The [filename] to remove.
     * @return true if successful, false if not.
     */
    override fun deleteFile(filename: String): Boolean {
        val file = resolveFullPathForFilename(filename)
        if (Files.exists(file)) {
            Files.delete(file)
            return true
        }
        return false
    }

    /**
     * Delete the file from storage.
     *
     * @param filename The [filename] to remove.
     * @param relativePathFromBase The [relativePathFromBase] path of the file.
     * @return true if successful, false if not.
     */
    override fun deleteFileAtPath(filename: String, relativePathFromBase: Path): Boolean {
        val file = resolveFullPathForFilenameAndRelativePath(filename, relativePathFromBase)
        if (Files.exists(file)) {
            Files.delete(file)
            return true
        }
        return false
    }

    /**
     * The function [getResource] retrieves the resource with the given [fileName] parameter.
     *
     * @param fileName The [fileName] of the file to retrieve.
     * @throws FileStorageException when the file does not exist or is not readable.
     * @return The [Resource] of the file requested.
     */
    override fun getResource(fileName: String): Resource {
        try {
            val file = resolveFullPathForFilename(fileName)

            if (!Files.exists(file)) {
                throw ResourceNotFoundException(validationMessages.get("file.store.file.exists", fileName))
            }

            return UrlResource(file.toUri())
        } catch (ex: MalformedURLException) {
            LOGGER.error("Unable to resolve file location: $fileName", ex)
            throw BadRequestException(
                validationMessages.get("file.store.file.resolve", fileName, ex.message.toString()),
            )
        }
    }

    /**
     * The function [getResourceAtPath] retrieves the resource with the given [fileName], [relativePathFromBase] parameters.
     *
     * @param fileName The [fileName] of the file to retrieve.
     * @param relativePathFromBase The [relativePathFromBase] path of the file.
     * @throws FileStorageException when the file does not exist or is not readable.
     * @return The [Resource] of the file requested.
     */

    override fun getResourceAtPath(fileName: String, relativePathFromBase: Path): Resource {
        try {
            val file = resolveFullPathForFilenameAndRelativePath(fileName, relativePathFromBase)

            if (!Files.exists(file)) {
                throw ResourceNotFoundException(validationMessages.get("file.store.file.exists", fileName))
            }

            return UrlResource(file.toUri())
        } catch (ex: MalformedURLException) {
            LOGGER.error("Unable to resolve file location: $fileName", ex)
            throw BadRequestException(
                validationMessages.get("file.store.file.resolve", fileName, ex.message.toString()),
            )
        }
    }

    /**
     * The [generateFileName] function generates a new file name (Unix Timestamp).
     *
     * @param file The [file] uploaded as a multipart request.
     * @return The new file name generated.
     */
    private fun generateFileName(file: MultipartFile): String {
        val extension = FilenameUtils.getExtension(file.originalFilename)
        var name = System.currentTimeMillis()
        var filename = "$name.$extension"

        while (Files.exists(resolveFullPathForFilename(filename))) {
            name = System.currentTimeMillis()
            filename = "$name.$extension"
        }
        return filename
    }

    /**
     * The function [resolveFullPathForFilename] resolves the full path for a given file name.
     *
     * @param fileName The [fileName] of the file.
     * @return The resolved path of the file requested.
     */
    private fun resolveFullPathForFilename(fileName: String): Path {
        return fileUtil.resolveFileStorage(fileName)
    }

    private fun resolveFullPathForFilenameAndRelativePath(fileName: String, relativePath: Path): Path {
        return fileUtil.resolveFileStoragePathForFilenameAndRelativePath(fileName, relativePath)
    }
}
