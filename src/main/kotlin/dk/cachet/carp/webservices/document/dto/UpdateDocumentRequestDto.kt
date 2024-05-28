package dk.cachet.carp.webservices.document.dto

import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * The Data Class [UpdateDocumentRequestDto].
 * The [UpdateDocumentRequestDto] represents an document request with the given [name] and [data].
 */
data class UpdateDocumentRequestDto(
    /** The [name] of the document. */
    @field:NotBlank
    val name: String?,
    /** The [data] object containing the document information. */
    @field:NotNull
    var data: JsonNode? = null,
)
