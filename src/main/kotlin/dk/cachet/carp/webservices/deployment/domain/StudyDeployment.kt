package dk.cachet.carp.webservices.deployment.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import dk.cachet.carp.webservices.common.audit.Auditable
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes

/**
 * The Data Class [StudyDeployment].
 * The [StudyDeployment] represents a study deployment with the given [id], [snapshot], and
 * [deployedFromStudyId] of the study.
 */
@Entity
@Table(name = "deployments")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class StudyDeployment
(
        /** The deployment [id]. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = 0,

        /** The study protocol [snapshot] as a JsonNode */
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        @Type(JsonBinaryType::class)
        var snapshot: JsonNode? = null,

        /** The [deployedFromStudyId] of the deployment. */
        var deployedFromStudyId: String? = null
): Auditable()