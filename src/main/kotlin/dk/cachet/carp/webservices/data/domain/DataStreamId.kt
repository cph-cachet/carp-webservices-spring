package dk.cachet.carp.webservices.data.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.common.audit.Auditable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.io.Serializable

@Entity(name = "data_stream_ids")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class DataStreamId(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    var studyDeploymentId: String? = "",
    var deviceRoleName: String? = "",
    var name: String? = "",
    var nameSpace: String? = "",
) : Auditable(), Serializable
