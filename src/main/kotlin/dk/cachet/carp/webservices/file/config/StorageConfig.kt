package dk.cachet.carp.webservices.file.config

import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.environment.EnvironmentProfile
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import dk.cachet.carp.webservices.file.util.FileUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * The Configuration Class [StorageConfig].
 * The [StorageConfig] enables application to create a file storage on application start.
 */
@Configuration
class StorageConfig(
    private val environmentUtil: EnvironmentUtil,
    private val fileUtil: FileUtil,
    private val validationMessages: MessageBase
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    init
    {
        createStorageOnStart()
    }

    /**
     * The function [createStorageOnStart] enables creating storage on application startup.
     */
    @Throws(IOException::class, FileStorageException::class)
    final fun createStorageOnStart()
    {
        val rootFolder: Path = when(environmentUtil.profile)
        {
            EnvironmentProfile.LOCAL -> Paths.get(fileUtil.filePath.toString()).toAbsolutePath().normalize()
            else -> Paths.get(fileUtil.storageDirectory.toString()).toAbsolutePath().normalize()
        }?: throw FileStorageException(validationMessages.get("file.directory.empty","file"))

        val directory: Path = rootFolder.let { fileUtil.isDirectoryOrElseCreate(it) }

        if (!fileUtil.isDirectory(directory))
        {
            throw FileStorageException(validationMessages.get("file.directory.exists","file"))
        }

        if (!fileUtil.isWritable(directory))
        {
            if (!fileUtil.setStoragePermission(directory))
            {
                throw FileStorageException(validationMessages.get("file.directory.error", "file"))
            }
            else if (fileUtil.setStoragePermission(directory))
            {
                LOGGER.info("File storage created: {}", directory)
            }
        }
        else LOGGER.info("File storage: {}", directory)
    }
}