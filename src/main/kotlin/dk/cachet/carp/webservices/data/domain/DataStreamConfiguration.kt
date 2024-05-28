package dk.cachet.carp.webservices.data.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity(name = "data_stream_configurations")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DataStreamConfiguration(
    @Id
    var studyDeploymentId: String? = "",
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var config: JsonNode? = null,
    var closed: Boolean = false,
)
