package dk.cachet.carp.webservices.export.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.common.audit.Auditable
import jakarta.persistence.Entity
import jakarta.persistence.Id

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
    var type: ExportType = ExportType.UNKNOWN,
) : Auditable()

enum class ExportStatus {
    UNKNOWN,
    IN_PROGRESS,
    AVAILABLE,
    ERROR,
    EXPIRED,
}

enum class ExportType {
    UNKNOWN,
    STUDY_DATA,
    DEPLOYMENT_DATA,
    ANONYMOUS_PARTICIPANTS,
}
