package dk.cachet.carp.webservices.file.scheduler

import dk.cachet.carp.webservices.file.service.FileService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FileCleanup(
    private val fileService: FileService,
    @Value("\${cleanup.files.retention-days}") private val days: Int,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    @Scheduled(cron = "0 0 9 * * ?")
    fun cleanup() {
        LOGGER.info("Cleaning up files...")
        fileService.deleteAllOlderThan(days)
    }
}
