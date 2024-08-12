package dk.cachet.carp.webservices.export.config

import dk.cachet.carp.webservices.export.domain.ExportStatus
import dk.cachet.carp.webservices.export.repository.ExportRepository
import dk.cachet.carp.webservices.file.service.FileStorage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration
import java.time.Instant

@Configuration
@ConditionalOnProperty(value = ["storage.exports.expiration.enabled"], havingValue = "true")
class ExportExpirationConfig(
    @Value("\${storage.exports.expiration.days}") private val expirationDays: Long,
    private val exportRepository: ExportRepository,
    private val fileStorage: FileStorage,
) {
    @Scheduled(cron = "@daily")
    fun expireOldExports() {
        exportRepository.findAllCreatedBefore(Instant.now().minus(Duration.ofDays(expirationDays)))
            .filter { it.status == ExportStatus.AVAILABLE }
            .forEach {
                exportRepository.updateExportStatus(
                    ExportStatus.EXPIRED,
                    it.id,
                )
                fileStorage.deleteFile(it.fileName)
            }
    }
}
