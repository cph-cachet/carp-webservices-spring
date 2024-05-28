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
 * The Configuration Class [LoggerConfig].
 * The [LoggerConfig] enables application to create a logger storage on application start.
 */
@Configuration
class LoggerConfig(
    private val environmentUtil: EnvironmentUtil,
    private val fileUtil: FileUtil,
    private val validationMessages: MessageBase,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    init
    {
        createLoggerStorageOnStart()
    }

    /**
     * The function [createLoggerStorageOnStart] creates a storage on application startup.
     */
    @Throws(IOException::class, FileStorageException::class)
    final fun createLoggerStorageOnStart() {
        val rootFolder: Path =
            when (environmentUtil.profile) {
                EnvironmentProfile.LOCAL -> Paths.get(fileUtil.filePath.toString()).toAbsolutePath().normalize()
                else -> Paths.get(fileUtil.storageDirectory.toString()).toAbsolutePath().normalize()
            } ?: throw FileStorageException(validationMessages.get("file.directory.empty", "logging"))

        val directory: Path = fileUtil.isDirectoryOrElseCreate(rootFolder)

        if (!fileUtil.isDirectory(directory)) {
            throw FileStorageException(validationMessages.get("file.directory.exists", "logging"))
        }

        if (!fileUtil.isWritable(directory)) {
            if (!fileUtil.setStoragePermission(directory)) {
                LOGGER.warn("Logger directory cannot be created.")
                throw FileStorageException(validationMessages.get("file.directory.error", "logging"))
            } else if (fileUtil.setStoragePermission(directory)) {
                LOGGER.info("Logging storage was created: {}", directory)
            }
        } else {
            LOGGER.info("Logging storage: {}", directory)
        }
    }
}
