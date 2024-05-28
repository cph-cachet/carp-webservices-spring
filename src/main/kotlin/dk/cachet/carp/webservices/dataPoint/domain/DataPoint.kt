package dk.cachet.carp.webservices.dataPoint.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.dataPoint.dto.DataPointHeaderDto
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * The Data Class [DataPoint].
 * The [DataPoint] represents a data point domain with the given [id], [deploymentId], [carpHeader], [carpBody], and [storageName].
 */
@Entity(name = "data_points")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@EntityListeners(AuditingEntityListener::class)
data class DataPoint(
    /** The data point [id]. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    /** The datapoint [deploymentId]. */
    var deploymentId: String? = null,
    /** The data point [carpHeader]. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType::class)
    var carpHeader: DataPointHeaderDto? = null,
    /** The data point [carpBody]. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType::class)
    var carpBody: HashMap<*, *>? = null,
    /** The data point [storageName]. */
    var storageName: String? = null,
    /** The [createdBy] creator identity. Contains the account id of the user. */
    var createdBy: String? = null,
    /** The [updatedBy] the ID of the user the entity was updated by.
     * Contains the account id of the user. */
    var updatedBy: String? = null,
    /** The [createdAt] time of creation. */
    @CreatedDate
    var createdAt: Instant? = null,
    /** The [updatedAt] last time the entity was updated. */
    @LastModifiedDate
    var updatedAt: Instant? = null,
) : Serializable
