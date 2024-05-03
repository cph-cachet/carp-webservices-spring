package dk.cachet.carp.webservices.export.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.audit.Auditable
import dk.cachet.carp.webservices.export.command.ExportCommand
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.apache.logging.log4j.LogManager
import java.time.Instant

/**
 * The Data Class [Export].
 * The [Export] is a database entry which indicates an exported resource (e.g. a study data export).
 */
@Entity(name = "exports")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Export(
    @Id
    var id: String = "",
    var fileName: String = "",
    var status: ExportStatus = ExportStatus.UNKNOWN,
    var studyId: String = "",
    var type: ExportType = ExportType.UNKNOWN
): Auditable()

enum class ExportStatus
{
    UNKNOWN,
    IN_PROGRESS,
    AVAILABLE,
    ERROR,
    EXPIRED
}

enum class ExportType
{
    UNKNOWN,
    STUDY_DATA,
    PARTICIPANT_GROUP_DATA,
    ANONYMOUS_PARTICIPANTS
}
