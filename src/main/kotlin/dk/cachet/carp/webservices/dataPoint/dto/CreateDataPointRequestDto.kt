package dk.cachet.carp.webservices.dataPoint.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.google.gson.annotations.SerializedName
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.jetbrains.annotations.Nullable

/**
 * The Data Class [CreateDataPointRequestDto].
 * The [CreateDataPointRequestDto] represents a data point request to a user with the given [carpHeader], [carpBody], and [storageName].
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateDataPointRequestDto(
    /** The [carpHeader] of the request. */
    @field:Valid
    @field:NotNull
    @SerializedName("carp_header")
    var carpHeader: DataPointHeaderDto? = null,
    /** The [carpBody] of the request. */
    @field:NotNull
    @SerializedName("carp_body")
    var carpBody: HashMap<*, *>? = null,
    /** The [storageName] of the request. */
    @field:Nullable
    @SerializedName("storage_name")
    var storageName: String? = null,
)
