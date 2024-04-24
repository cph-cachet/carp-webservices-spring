package dk.cachet.carp.webservices.file.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.common.audit.Auditable
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes

/**
 * The Data Class [File].
 * The [File] represents a file domain with the given [id], [storageName], [originalName], [metadata], and [studyId] of the study.
 */
@Entity(name = "files")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class File
(
        /** The file [id] field. */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = 0,

        /** The file [storageName] field. */
        @field:NotNull
        var storageName: String = "",

        /** The file [originalName] field. */
        @field:NotNull
        var originalName: String = "",

        /** The file [metadata] field. */
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        @Type(JsonBinaryType::class)
        var metadata: JsonNode? = null,

        /** The file [studyId] field. */
        @field:NotNull
        var studyId: String = ""
): Auditable()