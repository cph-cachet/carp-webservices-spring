package dk.cachet.carp.webservices.common.exception.file

/**
 * The Class [FileStorageException].
 * The [FileStorageException] is thrown if the file does not exist or cannot be accessed.
 */
class FileStorageException(message: String?) : RuntimeException(message)
