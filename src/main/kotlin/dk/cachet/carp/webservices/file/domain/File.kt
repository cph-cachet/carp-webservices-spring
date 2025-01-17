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

@Entity(name = "files")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class File(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @field:NotNull
    var fileName: String = "", // (storageName)
    @field:NotNull
    val relativePath: String = "", // relative path e.g. .../local/{relativePath}/{fileName}
    @field:NotNull
    var originalName: String = "",
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType::class)
    var metadata: JsonNode? = null,
    @field:NotNull
    var studyId: String = "",
    var ownerId: String? = null,
    var deploymentId: String? = null,
) : Auditable()
