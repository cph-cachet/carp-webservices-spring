package dk.cachet.carp.webservices.datastream.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.common.audit.Auditable
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.io.Serializable

@Entity(name = "data_stream_sequence")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DataStreamSequence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val dataStreamId: Int? = 0,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var snapshot: JsonNode? = null,
    var firstSequenceId: Long? = 0,
    var lastSequenceId: Long? = 0,
) : Auditable(), Serializable
