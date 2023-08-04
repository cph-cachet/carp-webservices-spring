package dk.cachet.carp.webservices.summary.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.common.audit.Auditable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * The Data Class [Summary].
 * The [Summary] is a database entry which indicates a zipped file in the file system
 * that contains data from a study.
 */
@Entity(name = "summary")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Summary
(
        @Id
        var id: String = "",

        var fileName: String = "",

        var status: SummaryStatus = SummaryStatus.UNKNOWN,

        var studyId: String = ""
): Auditable()